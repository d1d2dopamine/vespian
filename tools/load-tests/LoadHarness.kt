package dev.vespian.ui

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LoadHarness {
    @Before fun reset() = UiCache.reset()
    @Test fun successEndsLoading() = runBlocking {
        assertEquals(LoadState("fresh", false, false), loadState("old") { "fresh" })
    }
    @Test fun initialFailureEndsLoading() = runBlocking {
        val state = loadState<String>(null) { error("failure") }
        assertEquals(LoadState<String>(null, false, true), state)
    }
    @Test fun retryRecoversCachedFailure() = runBlocking {
        val failed = loadState("old") { error("failure") }
        assertEquals(LoadState("old", false, true), failed)
        assertEquals(LoadState("fresh", false, false), loadState(failed.value) { "fresh" })
    }
    @Test fun cancellationRethrowsSameObject() = runBlocking {
        val cancellation = CancellationException("cancel")
        try { loadResult<String> { throw cancellation }; fail("Cancellation swallowed") }
        catch (caught: CancellationException) { assertSame(cancellation, caught) }
    }
    @Test fun fatalErrorsPropagate() = runBlocking {
        val fatal = AssertionError("fatal")
        try { loadResult<String> { throw fatal }; fail("Fatal error swallowed") }
        catch (caught: AssertionError) { assertSame(fatal, caught) }
    }
    @Test fun cancelledBeforeStartDoesNotCallDependency() = runBlocking {
        var called = false
        val job = launch {
            currentCoroutineContext().cancel()
            loadResult { called=true }
        }
        job.join(); assertFalse(called)
    }
    @Test fun swallowedCancellationCannotPublishSuccess() = runBlocking {
        var published = false
        val job = launch {
            loadResult { currentCoroutineContext().cancel(); "late" }
            published = true
        }
        job.join(); assertFalse(published)
    }
    @Test fun cancellationDuringFailureCannotPublishFallback() = runBlocking {
        var published = false
        val job = launch {
            loadResult<String> { currentCoroutineContext().cancel(); error("late failure") }
            published = true
        }
        job.join(); assertFalse(published)
    }
    @Test fun todayInitialFailureIsRetryableState() = runBlocking {
        Engine.forecastCall={ error("failure") }
        val effects=Effects(); effects.today()
        assertEquals(LoadState<String>(null,false,true),effects.forecastLoad)
        assertNull(UiCache.forecast)
    }
    @Test fun todayCachedFailureAndSuccessfulRetry() = runBlocking {
        val effects=Effects(); effects.forecastLoad=LoadState("old"); UiCache.forecast="old"
        Engine.forecastCall={ error("failure") }; effects.today()
        assertEquals(LoadState("old",false,true),effects.forecastLoad); assertEquals("old",UiCache.forecast)
        Engine.forecastCall={ "new" }; effects.today()
        assertEquals(LoadState("new",false,false),effects.forecastLoad); assertEquals("new",UiCache.forecast)
    }
    @Test fun todayCancellationStopsAncillaryCalls() = runBlocking {
        var called=false
        Engine.forecastCall={ throw CancellationException() }; Engine.loadCall={ called=true; Filter() }
        try { Effects().today(); fail("swallowed") } catch (_: CancellationException) {}
        assertFalse(called); assertNull(UiCache.forecast)
    }
    @Test fun todayAncillaryCancellationDoesNotReplaceCache() = runBlocking {
        UiCache.latency=42.0
        Engine.loadCall={ throw CancellationException() }
        try { Effects().today(); fail("swallowed") } catch (_: CancellationException) {}
        assertEquals(42.0,UiCache.latency!!,0.0); assertNull(UiCache.obs)
    }
    @Test fun driftInitialFailureEndsLoading() = runBlocking {
        Engine.forecastCall={ error("failure") }; val effects=Effects(); effects.drift()
        assertEquals(LoadState<String>(null,false,true),effects.forecastLoad)
    }
    @Test fun driftCachedFailureIsMarkedAndRetained() = runBlocking {
        Engine.forecastCall={ error("failure") }; val effects=Effects(); effects.forecastLoad=LoadState("old")
        effects.drift(); assertEquals(LoadState("old",false,true),effects.forecastLoad)
    }
    @Test fun driftCancellationDoesNotPublish() = runBlocking {
        UiCache.forecast="old"; Engine.forecastCall={ throw CancellationException() }
        try { Effects().drift(); fail("swallowed") } catch (_: CancellationException) {}
        assertEquals("old",UiCache.forecast)
    }
    @Test fun historyFailureIsDifferentFromEmptySuccess() = runBlocking {
        val effects=Effects(); PredLog.call={ error("failure") }; effects.historyEffect()
        assertTrue(effects.historyLoad.failed); assertNull(effects.historyLoad.value)
        PredLog.call={ emptyList() }; effects.historyEffect()
        assertFalse(effects.historyLoad.failed); assertEquals(emptyList<String>(),effects.historyLoad.value)
    }
    @Test fun historyKeepsCachedRowsOnFailure() = runBlocking {
        val effects=Effects(); effects.historyLoad=LoadState(listOf("old")); UiCache.history=listOf("old")
        PredLog.call={ error("failure") }; effects.historyEffect()
        assertEquals(LoadState(listOf("old"),false,true),effects.historyLoad)
        assertEquals(listOf("old"),UiCache.history)
    }
    @Test fun historyCancellationDoesNotPublish() = runBlocking {
        UiCache.history=listOf("old"); PredLog.call={ throw CancellationException() }
        try { Effects().historyEffect(); fail("swallowed") } catch (_: CancellationException) {}
        assertEquals(listOf("old"),UiCache.history)
    }
    @Test fun modelInitialFailureEndsLoading() = runBlocking {
        Engine.loadCall={ error("failure") }; val effects=Effects(); effects.model()
        assertTrue(effects.modelLoad.failed); assertFalse(effects.modelLoad.loading); assertNull(effects.modelLoad.value)
    }
    @Test fun modelRejectsEmptyParticlesThenRecovers() = runBlocking {
        Engine.loadCall={ Filter(emptyList()) }; val effects=Effects(); effects.model()
        assertTrue(effects.modelLoad.failed); assertNull(UiCache.filter)
        Engine.loadCall={ Filter() }; effects.model()
        assertFalse(effects.modelLoad.failed); assertEquals(1,effects.modelLoad.value!!.first.particles.size)
        assertEquals(2,UiCache.nights)
    }
    @Test fun modelCountFailureKeepsConsistentCachedPair() = runBlocking {
        val old=Filter(); val effects=Effects(); effects.modelLoad=LoadState(old to 7)
        UiCache.filter=old; UiCache.nights=7; Db.countCall={ error("failure") }; effects.model()
        assertEquals(LoadState(old to 7,false,true),effects.modelLoad)
        assertSame(old,UiCache.filter); assertEquals(7,UiCache.nights)
    }
    @Test fun modelCancellationDoesNotPublish() = runBlocking {
        Engine.loadCall={ throw CancellationException() }
        try { Effects().model(); fail("swallowed") } catch (_: CancellationException) {}
        assertNull(UiCache.filter)
    }
    @Test fun supersededRefreshCannotOverwriteNewResult() = runBlocking {
        val entered=CompletableDeferred<Unit>(); val release=CompletableDeferred<Unit>(); var published="old"
        val old=launch {
            val result=loadState("old") {
                entered.complete(Unit)
                withContext(NonCancellable) { release.await() }
                "obsolete"
            }
            published=result.value!!
        }
        entered.await(); old.cancel(); published=loadState(published) { "new" }.value!!
        release.complete(Unit); old.join(); assertEquals("new",published)
    }
}
