package dev.vespian.model

import dev.vespian.health.HealthFreshness
import org.junit.Assert.*
import org.junit.Test

class HealthFreshnessTest {
    private val hour = 3_600_000L
    private val now = 1_800_000_000_000L
    @Test fun `no measurements have unknown age`() { assertNull(HealthFreshness.ageMillis(now, null)) }
    @Test fun `nonpositive and future observations are rejected`() {
        assertNull(HealthFreshness.ageMillis(now, 0))
        assertNull(HealthFreshness.ageMillis(now, -1))
        assertNull(HealthFreshness.ageMillis(now, now + 1))
    }
    @Test fun `fresh measurement has zero age`() { assertEquals(0L, HealthFreshness.ageMillis(now, now)) }
    @Test fun `before thirty hours no warning is due`() { assertNull(HealthFreshness.warningHours(now, now - 30 * hour + 1, null)) }
    @Test fun `exactly thirty hours reaches the threshold`() { assertEquals(30L, HealthFreshness.warningHours(now, now - 30 * hour, null)) }
    @Test fun `reimporting an old night cannot reset its age`() {
        val observedEnd = now - 40 * hour
        for (tick in 0..5) assertEquals(40L + tick, HealthFreshness.warningHours(now + tick * hour, observedEnd, null))
    }
    @Test fun `old download time policy hides the same stale night`() {
        // The former worker stored now into last_data_at for a re-read night.
        val legacyDownloadTime = now
        assertTrue(now - legacyDownloadTime < 30 * hour)
        assertEquals(40L, HealthFreshness.warningHours(now, now - 40 * hour, null))
    }
    @Test fun `warning is throttled within a day`() { assertNull(HealthFreshness.warningHours(now, now - 80 * hour, now - 24 * hour + 1)) }
    @Test fun `warning is allowed at exactly one day`() { assertEquals(80L, HealthFreshness.warningHours(now, now - 80 * hour, now - 24 * hour)) }
    @Test fun `new sleep stops an old stale warning`() { assertNull(HealthFreshness.warningHours(now, now - 8 * hour, now - 40 * hour)) }
    @Test fun `future throttle time after clock rollback cannot suppress forever`() { assertEquals(40L, HealthFreshness.warningHours(now, now - 40 * hour, now + hour)) }
    @Test fun `invalid old warning time does not block first warning`() { assertEquals(40L, HealthFreshness.warningHours(now, now - 40 * hour, -1)) }
    @Test fun `unknown measurements never create a stale warning`() { assertNull(HealthFreshness.warningHours(now, null, null)) }
}
