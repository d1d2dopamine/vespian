package dev.vespian.model

import android.content.Context
import dev.vespian.db.Db
import dev.vespian.db.Meta
import dev.vespian.db.Night
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZoneId
import kotlin.math.abs

/** Versioned, timestamped prospective promises. Legacy records remain untouched. */
object PredLog {
    // The old key lacks saved UTC/zone/provenance, and its logger admitted
    // daytime/post-outcome records. Preserve it for backups, never silently
    // promote those rows into prospective evaluation evidence.
    const val LEGACY_KEY = "pred_log"
    const val KEY = "pred_log_v2"
    private const val KEEP = 60
    private val recordLock = Mutex()

    data class Entry(val dateKey: String, val stamp: ForecastTiming.Record) {
        val at: Long get() = stamp.recordedAt
        // Display on the same current local axis as Row.actual. Scoring below
        // uses saved UTC directly and cannot change when the device travels.
        val low: Double get() = Engine.hourOf(stamp.lowUtc)
        val median: Double get() = Engine.hourOf(stamp.medianUtc)
        val high: Double get() = Engine.hourOf(stamp.highUtc)
    }

    data class Row(val dateKey: String, val night: Night, val pred: Entry?) {
        val actual: Double get() = Engine.hourOf(night.sleepStart)
        val hit: Boolean?
            get() = pred?.stamp?.let { night.sleepStart >= it.lowUtc && night.sleepStart <= it.highUtc }
        val errorMinutes: Int?
            get() = pred?.stamp?.let {
                Math.round((night.sleepStart.toDouble() - it.medianUtc.toDouble()) / 60_000.0)
                    .coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
            }
    }

    private fun parse(raw: String?): MutableList<Entry> {
        val out = mutableListOf<Entry>()
        if (raw.isNullOrBlank()) return out
        val arr = runCatching { JSONArray(raw) }.getOrNull() ?: return out
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val entry = runCatching {
                require(o.getInt("v") == ForecastTiming.FORMAT_VERSION)
                Entry(
                    o.getString("d"),
                    ForecastTiming.Record(
                        recordedAt = o.getLong("t"),
                        computedAt = o.getLong("computed"),
                        latestSleepEnd = o.getLong("latest_sleep_end"),
                        zoneId = o.getString("zone"),
                        cycleStart = o.getLong("cycle_start"),
                        cycleEnd = o.getLong("cycle_end"),
                        lowUtc = o.getLong("lo_utc"),
                        medianUtc = o.getLong("me_utc"),
                        highUtc = o.getLong("hi_utc"),
                        modelVersion = o.getInt("model_version"),
                        protocolVersion = o.getInt("protocol_version"),
                    ),
                ).also { require(ForecastTiming.valid(it.stamp, Long.MAX_VALUE)) }
            }.getOrNull()
            if (entry != null) out.add(entry)
        }
        return out
    }

    private fun encode(list: List<Entry>): String {
        val arr = JSONArray()
        for (e in list) {
            val s = e.stamp
            arr.put(JSONObject()
                .put("v", ForecastTiming.FORMAT_VERSION).put("d", e.dateKey)
                .put("t", s.recordedAt).put("computed", s.computedAt)
                .put("latest_sleep_end", s.latestSleepEnd).put("zone", s.zoneId)
                .put("cycle_start", s.cycleStart).put("cycle_end", s.cycleEnd)
                .put("lo_utc", s.lowUtc).put("me_utc", s.medianUtc).put("hi_utc", s.highUtc)
                .put("model_version", s.modelVersion).put("protocol_version", s.protocolVersion))
        }
        return arr.toString()
    }

    suspend fun all(context: Context): List<Entry> = withContext(Dispatchers.IO) {
        parse(Db.get(context).meta().get(KEY))
    }

    /** First frozen promise per evening cycle wins; never overwrite it after the outcome. */
    suspend fun record(context: Context, forecast: Forecast, now: Long) = recordLock.withLock {
        val zone = ZoneId.systemDefault()
        if (!ForecastTiming.isRecordingTime(now, zone) || forecast.nights <= 0) return@withLock
        val offset = forecast.utcOffsetHours ?: return@withLock
        val computed = forecast.computedAt ?: return@withLock
        val lo = ForecastTiming.utcMillis(forecast.onset.low, offset) ?: return@withLock
        val me = ForecastTiming.utcMillis(forecast.onset.median, offset) ?: return@withLock
        val hi = ForecastTiming.utcMillis(forecast.onset.high, offset) ?: return@withLock
        val cycle = ForecastTiming.cycle(now, zone)
        withContext(Dispatchers.IO) {
            val db = Db.get(context)
            val latestSleepEnd = db.nights().lastSleepEnd() ?: return@withContext
            val stamp = ForecastTiming.Record(now, computed, latestSleepEnd, zone.id,
                cycle.start, cycle.end, lo, me, hi, Engine.MODEL_VERSION)
            // Includes clock rollback/future observations and compute-after-record checks.
            if (!ForecastTiming.valid(stamp, now)) return@withContext
            val list = parse(db.meta().get(KEY))
            if (list.any { it.stamp.cycleStart == cycle.start && it.stamp.cycleEnd == cycle.end }) return@withContext
            list.add(Entry(cycle.key, stamp))
            list.sortBy { it.at }
            while (list.size > KEEP) list.removeAt(0)
            db.meta().put(Meta(KEY, encode(list)))
        }
    }

    suspend fun history(context: Context, n: Int, asOf: Long = System.currentTimeMillis()): List<Row> = withContext(Dispatchers.IO) {
        val db = Db.get(context)
        // Match before taking the display window: truncating first could hide
        // an earlier target and incorrectly substitute a later sleep for it.
        val nights = db.nights().all().sortedBy { it.sleepStart }
        val preds = parse(db.meta().get(KEY))
        val matched = if (ForecastTiming.PROTOCOL_APPROVED) {
            ForecastTiming.match(preds.map { it.stamp },
                nights.map { ForecastTiming.Sleep(it.sleepStart, it.sleepEnd) }, asOf)
        } else emptyMap()
        nights.indices.filter { nights[it].sleepEnd <= asOf }.takeLast(n.coerceAtLeast(0)).map { i ->
            Row(nights[i].dateKey, nights[i], matched[i]?.let { preds[it] })
        }
    }

    fun accuracy(rows: List<Row>): Double? {
        val graded = rows.mapNotNull { it.hit }
        return if (graded.isEmpty()) null else graded.count { it }.toDouble() / graded.size
    }

    fun typicalMiss(rows: List<Row>): Int? {
        val errs = rows.mapNotNull { it.errorMinutes }.map { abs(it.toLong()) }.sorted()
        return errs.getOrNull(errs.size / 2)?.coerceAtMost(Int.MAX_VALUE.toLong())?.toInt()
    }
}
