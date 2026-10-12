package dev.vespian.model

import android.content.Context
import dev.vespian.db.Db
import dev.vespian.db.Meta
import dev.vespian.db.Night
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.json.JSONArray
import java.time.Instant
import java.util.TimeZone

/** Runs real journal/JSON code against a deliberately fake in-memory DAO. */
class JournalHarness {
    private val context = Context()
    private val now = Instant.parse("2026-10-05T18:00:00Z").toEpochMilli()
    private val start = Instant.parse("2026-10-06T02:00:00Z").toEpochMilli()
    private lateinit var originalZone: TimeZone
    private fun night(start: Long, end: Long, key: String) = Night(key, start, end, 480, 0, 0, 0, null, null, null, null, importedAt = end)
    private fun forecast() = Forecast(Band(1.0, 0.0, 2.0),
        Band(start / 3_600_000.0, (start - 3_600_000) / 3_600_000.0, (start + 3_600_000) / 3_600_000.0),
        Band(1.0, 0.0, 2.0), null, 0.1, 5, 0.0, utcOffsetHours = 0.0, computedAt = now - 1)
    @Before fun prepare() {
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Db.reset()
        Db.current.nightRows.add(night(now - 18 * 3_600_000, now - 10 * 3_600_000, "2026-10-05"))
    }
    @After fun cleanup() { TimeZone.setDefault(originalZone) }

    @Test fun `real JSON roundtrip preserves frozen provenance`() = runBlocking {
        PredLog.record(context, forecast(), now)
        val entry = PredLog.all(context).single()
        assertEquals(now, entry.at)
        assertEquals(now - 1, entry.stamp.computedAt)
        assertEquals(start, entry.stamp.medianUtc)
        assertEquals(Engine.MODEL_VERSION, entry.stamp.modelVersion)
        assertEquals(ForecastTiming.PROTOCOL_VERSION, entry.stamp.protocolVersion)
        assertEquals("UTC", entry.stamp.zoneId)
    }
    @Test fun `legacy journal remains byte for byte untouched and ungraded`() = runBlocking {
        val legacy = "[{\"d\":\"2026-10-05\",\"t\":42,\"lo\":1,\"me\":2,\"hi\":3}]"
        Db.current.metaRows[PredLog.LEGACY_KEY] = legacy
        assertTrue(PredLog.all(context).isEmpty())
        PredLog.record(context, forecast(), now)
        assertEquals(legacy, Db.current.metaRows[PredLog.LEGACY_KEY])
        assertEquals(1, PredLog.all(context).size)
    }
    @Test fun `later same cycle write does not replace first promise`() = runBlocking {
        PredLog.record(context, forecast(), now)
        val saved = Db.current.metaRows[PredLog.KEY]
        PredLog.record(context, forecast().copy(computedAt = now + 1), now + 3_600_000)
        assertEquals(saved, Db.current.metaRows[PredLog.KEY])
    }
    @Test fun `daytime is rejected at journal boundary too`() = runBlocking {
        PredLog.record(context, forecast().copy(computedAt = now - 8 * 3_600_000), now - 6 * 3_600_000)
        assertTrue(PredLog.all(context).isEmpty())
    }
    @Test fun `unfrozen forecast cannot enter the journal`() = runBlocking {
        PredLog.record(context, forecast().copy(utcOffsetHours = null), now)
        PredLog.record(context, forecast().copy(computedAt = null), now)
        assertTrue(PredLog.all(context).isEmpty())
    }
    @Test fun `known future sleep input prevents recording`() = runBlocking {
        Db.current.nightRows.add(night(start, start + 8 * 3_600_000, "2026-10-06"))
        PredLog.record(context, forecast(), now)
        assertTrue(PredLog.all(context).isEmpty())
    }
    @Test fun `unapproved protocol never feeds displayed history or calibration`() = runBlocking {
        assertFalse(ForecastTiming.PROTOCOL_APPROVED)
        PredLog.record(context, forecast(), now)
        Db.current.nightRows.add(night(start, start + 8 * 3_600_000, "2026-10-06"))
        val rows = PredLog.history(context, 30, now + 2 * 86_400_000)
        assertEquals(2, rows.size)
        assertTrue(rows.all { it.pred == null })
        assertEquals(0, Calib.score(context).graded)
        assertEquals(1.0, Calib.score(context).scale, 0.0)
    }
    @Test fun `unknown protocol version remains ungraded`() = runBlocking {
        PredLog.record(context, forecast(), now)
        val array = JSONArray(Db.current.metaRows[PredLog.KEY])
        array.getJSONObject(0).put("protocol_version", 999)
        Db.current.metaRows[PredLog.KEY] = array.toString()
        assertTrue(PredLog.all(context).isEmpty())
    }
    @Test fun `same wake date storage limitation is reproduced rather than hidden`() = runBlocking {
        val first = night(now + 2 * 3_600_000, now + 7 * 3_600_000, "2026-10-06")
        val second = night(now + 12 * 3_600_000, now + 20 * 3_600_000, "2026-10-06")
        // Mirrors NightDao REPLACE on the actual Night.dateKey primary key.
        val stored = linkedMapOf<String, Night>()
        stored[first.dateKey] = first
        stored[second.dateKey] = second
        assertEquals(listOf(second), stored.values.toList())
        assertFalse(ForecastTiming.PROTOCOL_APPROVED)
    }
}
