package dev.vespian.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** UI-only state; retaining a value is not evidence that its refresh succeeded. */
internal data class LoadState<T>(
    val value: T?,
    val loading: Boolean = true,
    val failed: Boolean = false,
)

/** Recover ordinary load errors, but never turn cancellation into fallback data. */
internal suspend fun <T> loadResult(block: suspend () -> T): Result<T> {
    currentCoroutineContext().ensureActive()
    return try {
        val value = block()
        // Also defend against a lower layer that swallowed cancellation.
        currentCoroutineContext().ensureActive()
        Result.success(value)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        currentCoroutineContext().ensureActive()
        Result.failure(error)
    }
}

internal suspend fun <T : Any> loadState(previous: T?, block: suspend () -> T): LoadState<T> =
    loadResult(block).fold(
        onSuccess = { LoadState(it, loading = false) },
        onFailure = { LoadState(previous, loading = false, failed = true) },
    )
