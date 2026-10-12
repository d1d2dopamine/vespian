package dev.vespian.model

import java.time.Instant
import java.time.ZoneId
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

class ForecastTimingTest {
    private val utc = ZoneId.of("UTC")
    private fun t(value: String) = Instant.parse(value).toEpochMilli()
    private val evening = t("2026-10-12T18:00:00Z")
    private val asOf = t("2026-10-14T18:00:00Z")
    private val sleep = ForecastTiming.Sleep(t("2026-10-13T02:00:00Z"), t("2026-10-13T10:00:00Z"))
    private fun record(at: Long = evening, zone: ZoneId = utc): ForecastTiming.Record {
        val cycle = ForecastTiming.cycle(at, zone)
        return ForecastTiming.Record(at, at - 1, evening - 36_000_000L, zone.id,
            cycle.start, cycle.end, sleep.start - 3_600_000, sleep.start, sleep.start + 3_600_000, 11)
    }
    private fun matches(r: ForecastTiming.Record = record(), sleeps: List<ForecastTiming.Sleep> = listOf(sleep), now: Long = asOf) =
        ForecastTiming.match(listOf(r), sleeps, now)

    @Test fun `modern daytime is not an evening`() {
        assertFalse(ForecastTiming.isRecordingTime(t("2026-10-12T12:00:00Z"), utc))
    }
    @Test fun `recording boundaries are 18 inclusive and 04 exclusive`() {
        assertFalse(ForecastTiming.isRecordingTime(t("2026-10-12T17:59:59Z"), utc))
        assertTrue(ForecastTiming.isRecordingTime(evening, utc))
        assertTrue(ForecastTiming.isRecordingTime(t("2026-10-13T03:59:59Z"), utc))
        assertFalse(ForecastTiming.isRecordingTime(t("2026-10-13T04:00:00Z"), utc))
    }
    @Test fun `gate uses the requested zone not epoch hours`() {
        val noon = t("2026-10-12T12:00:00Z")
        assertTrue(ForecastTiming.isRecordingTime(noon, ZoneId.of("Asia/Tokyo")))
        assertFalse(ForecastTiming.isRecordingTime(noon, utc))
    }
    @Test fun `midnight and 04 do not split the target cycle`() {
        val cycle = ForecastTiming.cycle(evening, utc)
        assertEquals(cycle, ForecastTiming.cycle(t("2026-10-13T03:00:00Z"), utc))
        assertEquals(cycle, ForecastTiming.cycle(t("2026-10-13T06:00:00Z"), utc))
    }
    @Test fun `next evening starts a new cycle`() {
        val cycle = ForecastTiming.cycle(evening, utc)
        assertNotEquals(cycle, ForecastTiming.cycle(cycle.end, utc))
    }
    @Test fun `spring cycle spans 23 actual hours`() {
        val cycle = ForecastTiming.cycle(t("2026-03-07T23:00:00Z"), ZoneId.of("America/New_York"))
        assertEquals(23 * 3_600_000L, cycle.end - cycle.start)
    }
    @Test fun `autumn cycle spans 25 actual hours`() {
        val cycle = ForecastTiming.cycle(t("2026-10-31T22:00:00Z"), ZoneId.of("America/New_York"))
        assertEquals(25 * 3_600_000L, cycle.end - cycle.start)
    }
    @Test fun `prospective finished sleep matches`() { assertEquals(mapOf(0 to 0), matches()) }
    @Test fun `sleep beginning after 04 still matches preceding evening`() {
        val late = ForecastTiming.Sleep(t("2026-10-13T06:00:00Z"), t("2026-10-13T14:00:00Z"))
        assertEquals(mapOf(0 to 0), matches(sleeps = listOf(late)))
    }
    @Test fun `record made exactly at sleep onset is not prospective`() {
        assertTrue(matches(record(sleep.start)).isEmpty())
    }
    @Test fun `record after sleep onset is excluded`() {
        assertTrue(matches(record(sleep.start + 60_000)).isEmpty())
    }
    @Test fun `later sleep never substitutes for a target already begun`() {
        val second = ForecastTiming.Sleep(t("2026-10-13T12:00:00Z"), t("2026-10-13T15:00:00Z"))
        assertTrue(matches(record(sleep.start + 60_000), listOf(second, sleep)).isEmpty())
    }
    @Test fun `future recordings are excluded after clock rollback`() {
        assertTrue(matches(now = evening - 1).isEmpty())
    }
    @Test fun `computation cannot be later than the record`() {
        assertTrue(matches(record().copy(computedAt = evening + 1)).isEmpty())
    }
    @Test fun `future input observation invalidates the record`() {
        assertTrue(matches(record().copy(latestSleepEnd = evening + 1)).isEmpty())
    }
    @Test fun `future and unfinished sleeps are not graded`() {
        assertTrue(matches(now = sleep.start - 1).isEmpty())
        assertTrue(matches(now = sleep.end - 1).isEmpty())
        assertEquals(mapOf(0 to 0), matches(now = sleep.end))
    }
    @Test fun `target end boundary is exclusive`() {
        val end = record().cycleEnd
        assertTrue(matches(sleeps = listOf(ForecastTiming.Sleep(end, end + 3_600_000))).isEmpty())
    }
    @Test fun `matching does not move to another cycle`() {
        val tomorrow = ForecastTiming.Sleep(sleep.start + 86_400_000, sleep.end + 86_400_000)
        assertTrue(matches(sleeps = listOf(tomorrow)).isEmpty())
    }
    @Test fun `first frozen promise wins regardless of input order`() {
        val first = record()
        val second = record(evening + 3_600_000)
        assertEquals(mapOf(0 to 1), ForecastTiming.match(listOf(second, first), listOf(sleep), asOf))
    }
    @Test fun `bad predicted time does not cherry pick another observation`() {
        val wrong = record().copy(lowUtc = evening - 3_600_000, medianUtc = evening, highUtc = evening + 3_600_000)
        assertEquals(mapOf(0 to 0), matches(wrong))
    }
    @Test fun `UTC conversion uses forecast offset`() {
        val instant = t("2026-10-13T02:00:00Z")
        assertEquals(instant, ForecastTiming.utcMillis(instant / 3_600_000.0 + 5.5, 5.5))
    }
    @Test fun `travel cannot change saved matching`() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"))
            val first = matches()
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            assertEquals(first, matches())
        } finally { TimeZone.setDefault(original) }
    }
    @Test fun `invalid provenance is not silently accepted`() {
        assertTrue(matches(record().copy(zoneId = "unknown/zone")).isEmpty())
        assertTrue(matches(record().copy(cycleEnd = record().cycleEnd + 1)).isEmpty())
        assertTrue(matches(record().copy(lowUtc = record().highUtc + 1)).isEmpty())
        assertTrue(matches(record().copy(computedAt = 0)).isEmpty())
        assertTrue(matches(record().copy(protocolVersion = 999)).isEmpty())
    }
    @Test fun `short naps do not replace the first long sleep target`() {
        val nap = ForecastTiming.Sleep(evening + 3_600_000, evening + 5_400_000)
        assertEquals(mapOf(1 to 0), matches(sleeps = listOf(nap, sleep)))
    }
    @Test fun `a two hour session meets the existing night threshold`() {
        val shortNight = ForecastTiming.Sleep(sleep.start, sleep.start + 7_200_000)
        assertEquals(mapOf(0 to 0), matches(sleeps = listOf(shortNight)))
    }
    @Test fun `nonfinite local times do not become plausible UTC`() {
        assertNull(ForecastTiming.utcMillis(Double.NaN, 0.0))
        assertNull(ForecastTiming.utcMillis(Double.POSITIVE_INFINITY, 0.0))
        assertNull(ForecastTiming.utcMillis(10.0, Double.NaN))
        assertNull(ForecastTiming.utcMillis(10.0, 19.0))
    }
}
