# Stage 7: finish widget broadcasts on failure and cancellation

## Scope and defect

Stage 7 is a separate copy of finalized stage 6. Only the widget rendering lifecycle is changed; no model, scoring, worker scheduling, coroutine ownership, database, or health-data behavior is redesigned.

Previously the coroutine acquired through `goAsync()` called `pending.finish()` only after `build()` returned. A resource/formatting exception could exit early and leave the broadcast unfinished. The nested `runCatching` around `Engine.forecast()` also swallowed cancellation and fatal errors, potentially publishing an unavailable widget after cancellation.

Android requires finishing the pending result after asynchronous processing. Moving work off-thread does not remove broadcast execution time limits. Primary reference: [BroadcastReceiver.goAsync](https://developer.android.com/reference/android/content/BroadcastReceiver#goAsync()).

## Repair

- One `finally` owns `pending.finish()` for success, ordinary failure, cancellation, and propagated fatal errors inside the launched body.
- Ordinary forecast/formatting failures attempt the existing unavailable presentation with the launch-app click intent and blank range/gate fields.
- If fallback creation or widget delivery also fails, a fixed generic warning is emitted and the pending result is still finished.
- Cancellation is rethrown and never deliberately converted into an unavailable update. Active-context checks before each widget update and in ordinary exception handlers also block publication and fallback/logging if a lower layer swallowed cancellation or translated it into an ordinary exception. Fatal errors are not caught as ordinary failures.
- At most two fixed log messages per render attempt; no exception object, exception text, token, forecast, or health payload is logged.
- Empty-ID early return, existing refresh broadcast, and zero-night presentation guard are preserved.

## Verification

`tools/test-widget-offline.py` extracts the unchanged launch body and build/helper implementations directly from the selected production source. It compiles these against explicit fake Android, Engine, and forecast boundaries using existing Kotlin/JUnit/coroutines tooling. Pass `--legacy /path/to/vespian-stage6/vespian-main` to reproduce regression failures. The harness prints source SHA-256 values.

Thirteen tests cover success; negative nights; zero nights; Engine failure; formatting failure; fallback failure; widget-update failure; thrown cancellation; cancellation during suspension; lower-layer swallowed cancellation; cancellation translated to an ordinary exception; fatal-error propagation; and bounded payload-free diagnostics. Every path checks exactly-once finish where applicable. Cancellation tests also require no widget publication.

The existing eight-test cold-start suite also runs with conditional extraction of the new widget helpers. Its failure-case test now checks Telegram only: widget failure recovery moved from private `build` to the render boundary and is exercised by the new widget harness, rather than assuming that private `build` swallows exceptions. Legacy cold-source extraction remains supported.

`tools/test-widget-wiring.py` checks the provider-facing boundaries omitted by extraction: empty IDs before `goAsync`, one finish in `finally`, cancellation rethrows, bounded exception catches, zero-night guard, and retained refresh/click wiring.

Compiler and JVM runs use one available CPU, nice 15, `ActiveProcessorCount=1`, and a 384 MiB heap. No downloads, SDK, emulator, live broadcasts, credentials, or health records are used.

### Results (2026-10-12 UTC)

- Baseline stage-6 widget body: 13 tests run, 8 failures (expected, exit 1). The formatting and fallback-resource exceptions escape before the old finish point; cancellation is swallowed/published; a fatal error is swallowed; fixed diagnostics are absent. The original source is unchanged.
- Stage-7 widget body: **13/13 pass** (exit 0).
- Cold-start actual-body regression suite: **8/8 pass** (exit 0).
- Widget static wiring: pass.
- Existing cold wiring: **19 checks pass**.
- Existing load, freshness, backup-secret, and semantics/nullables wiring scripts: pass (the semantics tool reports pre-existing informational symbol candidates, no failures).

Run with the already available `VESPIAN_JAVA_HOME`, `VESPIAN_GRADLE_HOME`, and `VESPIAN_COROUTINES_JAR` environment variables:

```sh
python tools/test-widget-offline.py --legacy /path/to/vespian-stage6/vespian-main  # expected exit 1
python tools/test-widget-offline.py
python tools/test-cold-offline.py
python tools/test-widget-wiring.py
```

Verified source SHA-256:

- Stage 6 ForecastWidget.kt: `eb2cb2be83abb183eab61a2398006788a33b80b6f62225555541b62b9f73a26d`
- Stage 7 ForecastWidget.kt: `9f6980498d2a653d1fb7276eb52c6cf77f1ca916120e10dad33a3ab1b9911896`


## Limits

This is a JVM extracted-body regression harness, not a full Android build or launcher/device test. It does not exercise actual `BroadcastReceiver` dispatch, `goAsync`, Android resource inflation, PendingIntent delivery, process lifecycle, or real RemoteViews. Fake boundary behavior is intentionally explicit.

The `finally` fixes normal completion/error/cancellation paths once the coroutine body executes. It cannot guarantee cleanup after process death, indefinite blocking/hung model work, failure before coroutine execution, or an OS broadcast deadline. No timeout/scheduling redesign is included. If widget delivery fails, the launcher may retain its earlier content. No medical accuracy or forecast validity claim is made.

## Included evidence and preserved source

Logs: `stage7-widget-baseline.txt`, `stage7-widget-fixed.txt`, `stage7-cold.txt`,
and `stage7-wiring.txt`. `stage7-unchanged.txt` lists the unchanged previous files.
Earlier numerical/journal/storage/load Kotlin suites were not rerun in this stage.
Original GPL/MIT notices and the public development-only signing fixture remain
unchanged; no production signing guarantee. Original and earlier archives remain
source rollback points, without an installed-app downgrade guarantee.
