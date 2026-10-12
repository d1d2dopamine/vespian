package dev.vespian.work

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WidgetHarness {
    private val context=Context()
    private val manager=AppWidgetManager()
    private val pending=Pending()
    private val body=WidgetBody()
    @Before fun reset() { Engine.action={Forecast()}; Engine.failFormatting=false; Log.entries.clear() }
    private fun render() = runBlocking { body.render(context,manager,intArrayOf(10,20),pending) }
    private fun unavailable() {
        assertEquals(2,manager.updates.size)
        manager.updates.forEach { (_,v) ->
            assertEquals("Unavailable",v.text[R.id.widget_value])
            assertEquals("",v.text[R.id.widget_range]); assertEquals("",v.text[R.id.widget_gate])
            assertTrue(v.clickable)
        }
    }
    @Test fun successFinishesOnce() { render(); assertEquals(1,pending.finishes); assertEquals(2,manager.updates.size); assertNotEquals("Unavailable",manager.updates[0].second.text[R.id.widget_value]) }
    @Test fun negativeNightsRemainUnavailable() { Engine.action={Forecast(nights=-1)}; render(); unavailable(); assertEquals(1,pending.finishes) }
    @Test fun zeroNightsRemainUnavailable() { Engine.action={Forecast(nights=0)}; render(); unavailable(); assertEquals(1,pending.finishes) }
    @Test fun engineFailureHasUnavailableFallback() { Engine.action={throw IllegalStateException("secret-token")}; render(); unavailable(); assertEquals(1,pending.finishes) }
    @Test fun formattingFailureHasUnavailableFallback() { Engine.failFormatting=true; render(); unavailable(); assertEquals(1,pending.finishes) }
    @Test fun fallbackFailureStillFinishes() { Engine.action={throw IllegalStateException("secret-token")}; context.failText=true; render(); assertEquals(1,pending.finishes); assertTrue(manager.updates.isEmpty()) }
    @Test fun updateFailureStillFinishes() { manager.fail=true; render(); assertEquals(1,pending.finishes) }
    @Test fun thrownCancellationPropagatesWithoutUpdate() {
        val cancellation=CancellationException("private-health-payload")
        Engine.action={throw cancellation}
        try { render(); fail("Cancellation swallowed") } catch (actual: CancellationException) { assertSame(cancellation,actual) }
        assertEquals(1,pending.finishes); assertTrue(manager.updates.isEmpty()); assertTrue(Log.entries.isEmpty())
    }
    @Test fun suspendedCancellationFinishesOnce() = runBlocking {
        val entered=CompletableDeferred<Unit>()
        Engine.action={entered.complete(Unit); awaitCancellation()}
        val job=launch { body.render(context,manager,intArrayOf(10,20),pending) }
        entered.await(); job.cancelAndJoin()
        assertEquals(1,pending.finishes); assertTrue(manager.updates.isEmpty()); assertTrue(Log.entries.isEmpty())
    }
    @Test fun swallowedLowerLayerCancellationDoesNotPublish() = runBlocking {
        val entered=CompletableDeferred<Unit>()
        Engine.action={
            entered.complete(Unit)
            try { awaitCancellation() } catch (_: CancellationException) { Forecast() }
        }
        val job=launch { body.render(context,manager,intArrayOf(10,20),pending) }
        entered.await(); job.cancelAndJoin()
        assertEquals(1,pending.finishes); assertTrue(manager.updates.isEmpty()); assertTrue(Log.entries.isEmpty())
    }
    @Test fun cancellationTranslatedToOrdinaryErrorDoesNotFallbackOrLog() = runBlocking {
        val entered=CompletableDeferred<Unit>()
        Engine.action={
            entered.complete(Unit)
            try { awaitCancellation() } catch (_: CancellationException) { error("secret-token") }
        }
        val job=launch { body.render(context,manager,intArrayOf(10,20),pending) }
        entered.await(); job.cancelAndJoin()
        assertEquals(1,pending.finishes); assertTrue(manager.updates.isEmpty()); assertTrue(Log.entries.isEmpty())
    }
    @Test fun fatalErrorPropagatesAndFinishes() {
        val error=AssertionError("fatal")
        Engine.action={throw error}
        try { render(); fail("Fatal error swallowed") } catch(actual: AssertionError) { assertSame(error,actual) }
        assertEquals(1,pending.finishes); assertTrue(manager.updates.isEmpty()); assertTrue(Log.entries.isEmpty())
    }
    @Test fun diagnosticsAreBoundedAndPayloadFree() {
        Engine.action={throw IllegalStateException("secret-token private-health-payload")}; manager.fail=true
        render()
        assertEquals(listOf("ForecastWidget: Widget forecast unavailable", "ForecastWidget: Widget update failed"),Log.entries)
        assertEquals(1,pending.finishes)
    }
}
