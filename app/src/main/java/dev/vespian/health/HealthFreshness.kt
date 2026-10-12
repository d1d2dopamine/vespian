package dev.vespian.health

/** Measurement age, independent of when an old record was downloaded again. */
object HealthFreshness {
    private const val HOUR_MS = 3_600_000L
    private const val DAY_MS = 24 * HOUR_MS
    private const val STALE_AFTER_MS = 30 * HOUR_MS

    fun ageMillis(now: Long, lastSleepEnd: Long?): Long? {
        if (now <= 0 || lastSleepEnd == null || lastSleepEnd <= 0 || lastSleepEnd > now) return null
        return now - lastSleepEnd
    }

    /** Null means no warning is due; no measurements is not proof of a stale feed. */
    fun warningHours(now: Long, lastSleepEnd: Long?, lastWarned: Long?): Long? {
        val age = ageMillis(now, lastSleepEnd) ?: return null
        if (age < STALE_AFTER_MS) return null
        // A future warning timestamp after clock rollback must not suppress
        // notices indefinitely. The next successful warning stores current time.
        if (lastWarned != null && lastWarned > 0 && lastWarned <= now && now - lastWarned < DAY_MS) return null
        return age / HOUR_MS
    }
}
