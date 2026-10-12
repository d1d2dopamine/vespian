package dev.vespian.model

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/** Pure timestamp policy for prospective evaluation, not a biological model. */
object ForecastTiming {
    const val FORMAT_VERSION = 2
    const val PROTOCOL_VERSION = 1
    // Candidate protocol only: its target definition needs owner agreement
    // before real validation. Do not let unapproved matches change confidence.
    const val PROTOCOL_APPROVED = false
    // Matches HealthRepo's current >=120-minute night definition.
    private const val MIN_NIGHT_MS = 120L * 60_000L

    data class Cycle(val key: String, val start: Long, val end: Long)
    data class Sleep(val start: Long, val end: Long)
    data class Record(
        val recordedAt: Long,
        val computedAt: Long,
        val latestSleepEnd: Long,
        val zoneId: String,
        val cycleStart: Long,
        val cycleEnd: Long,
        val lowUtc: Long,
        val medianUtc: Long,
        val highUtc: Long,
        val modelVersion: Int,
        val protocolVersion: Int = PROTOCOL_VERSION,
    )

    fun isRecordingTime(now: Long, zone: ZoneId): Boolean {
        val hour = Instant.ofEpochMilli(now).atZone(zone).hour
        return hour >= 18 || hour < 4
    }

    /**
     * An evening predicts the first long sleep starting before the next 18:00.
     * This fixed civil-time window is independent of the predicted value, so
     * a miss across midnight/04:00 cannot select a more convenient target.
     * Use calendar dates, not a fixed 24h duration, across daylight-saving days.
     */
    fun cycle(now: Long, zone: ZoneId): Cycle {
        val local = Instant.ofEpochMilli(now).atZone(zone)
        val day = if (local.hour < 18) local.toLocalDate().minusDays(1) else local.toLocalDate()
        return Cycle(
            day.toString(),
            day.atTime(LocalTime.of(18, 0)).atZone(zone).toInstant().toEpochMilli(),
            day.plusDays(1).atTime(LocalTime.of(18, 0)).atZone(zone).toInstant().toEpochMilli(),
        )
    }

    /** Freeze the model's local-hour axis into UTC using its own saved offset. */
    fun utcMillis(localHour: Double, offsetHours: Double): Long? {
        if (!localHour.isFinite() || !offsetHours.isFinite() || offsetHours !in -18.0..18.0) return null
        val value = (localHour - offsetHours) * 3_600_000.0
        if (!value.isFinite() || value < 0.0 || value >= Long.MAX_VALUE.toDouble()) return null
        return Math.round(value)
    }

    fun valid(record: Record, asOf: Long): Boolean {
        if (record.recordedAt <= 0 || record.recordedAt > asOf || record.computedAt <= 0 ||
            record.computedAt > record.recordedAt || record.latestSleepEnd <= 0 ||
            record.latestSleepEnd > record.computedAt || record.modelVersion <= 0 ||
            record.protocolVersion != PROTOCOL_VERSION ||
            record.lowUtc <= 0 || record.lowUtc > record.medianUtc || record.medianUtc > record.highUtc) return false
        val zone = runCatching { ZoneId.of(record.zoneId) }.getOrNull() ?: return false
        if (!isRecordingTime(record.recordedAt, zone)) return false
        val expected = cycle(record.recordedAt, zone)
        return record.cycleStart == expected.start && record.cycleEnd == expected.end
    }

    /**
     * Original sleep index -> original record index. First promise wins.
     * Consider the first sleep in each saved cycle BEFORE checking eligibility:
     * if it began before recording, never substitute a later sleep for it.
     * Unfinished/future nights, future recordings, and post-onset records cannot
     * contribute to evaluation. Matching is independent of current time zone.
     */
    fun match(records: List<Record>, sleeps: List<Sleep>, asOf: Long): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val orderedSleeps = sleeps.indices.filter {
            sleeps[it].start > 0 && sleeps[it].end > sleeps[it].start &&
                sleeps[it].end - sleeps[it].start >= MIN_NIGHT_MS
        }
            .sortedBy { sleeps[it].start }
        for (r in records.indices.sortedBy { records[it].recordedAt }) {
            val record = records[r]
            if (!valid(record, asOf)) continue
            val index = orderedSleeps.firstOrNull {
                sleeps[it].start >= record.cycleStart && sleeps[it].start < record.cycleEnd
            } ?: continue
            val sleep = sleeps[index]
            if (record.recordedAt >= sleep.start || sleep.end > asOf) continue
            result.putIfAbsent(index, r)
        }
        return result
    }
}
