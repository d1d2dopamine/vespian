package dev.vespian.ui
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Each failing assertion describes the behavior required after repair. */
class LegacyLoadHarness {
    @Before fun reset() = UiCache.reset()
    @Test fun todayMustNotSwallowCancellation() = runBlocking {
        Engine.forecastCall={ throw CancellationException() }
        var cancelled=false
        try { Effects().today() } catch (_: CancellationException) { cancelled=true }
        assertTrue("Actual stage-4 Today effect swallowed cancellation",cancelled)
    }
    @Test fun driftMustHaveTerminalFailureState() = runBlocking {
        Engine.forecastCall={ error("failure") }; val effects=Effects(); effects.drift()
        assertTrue("Actual stage-4 Drift leaves null forecast with no failure state",effects.failed)
    }
    @Test fun historyMustNotReplaceFailureWithNullEmptySentinel() = runBlocking {
        PredLog.call={ error("failure") }; val effects=Effects(); effects.history=listOf("old"); effects.drift()
        assertNotNull("Actual stage-4 history failure writes null, rendered as empty",effects.history)
    }
    @Test fun modelMustRejectEmptyParticles() = runBlocking {
        Engine.loadCall={ Filter(emptyList()) }; val effects=Effects(); effects.model()
        assertNull("Actual stage-4 Model publishes empty filter before unchecked median indexing",effects.filter)
    }
}
