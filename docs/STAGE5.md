# Stage 5: terminal load states and cancellation boundaries

Experimental source snapshot, 2026-10-12. Includes stages 1–4. No Android SDK,
Android build, device run, live requests, downloads, user data or migrations.

## Scope and repairs

- `MainActivity.kt`: Today, Drift and Model now distinguish initial loading,
  success and terminal failure. Initial failure presents an explicit Retry instead
  of an indefinite spinner. Retained values are visibly labelled while refreshing
  and after refresh failure. Retry is an effect key; a successful retry replaces
  the cached value and clears its error. Existing layouts/model outputs remain.
- `ui/LoadState.kt`: a pure generic state and suspend load helper. Ordinary
  Exceptions become failures; CancellationException is rethrown unchanged and
  fatal JVM Errors propagate. Coroutine activity is checked before calling a
  dependency and before publishing either success or failure. A cancelled older
  request cannot publish after a lower layer returns late.
- Today's ancillary loads use the same cancellation-safe result helper. Their
  existing fallback behavior for ordinary errors is deliberately unchanged.
- Drift history has its own load state and Retry, independent of forecast Retry.
  Failure retains valid cached history with an explicit warning. Without cache,
  failure displays an error; loading displays loading text. Only a successful
  empty list (or an explicitly labelled cached empty list) reaches the empty
  history message. `HistoryCard` now requires a non-null list.
- Model loads a validated non-empty particle set and its night count as a single
  display pair. A failure in either part retains the previous pair with a warning;
  an invalid empty cached filter is not rendered. The unchecked median indexing
  is reachable only after the non-empty validation. No numerical algorithm changed.
- Both existing string-resource variants contain the nine new Russian labels.
  Retry makes another local load attempt; it does not reset or delete data.

## Actual stage-4 baseline defects

`tools/test-load-offline.py --legacy /path/to/stage4/vespian-main` extracts and
compiles the original, unchanged LaunchedEffect bodies. Explicit fake Engine/DAO
boundaries inject deterministic outcomes. Four expected failing assertions show:

1. Today catches a thrown CancellationException and continues subsequent work.
2. Drift forecast failure leaves a null forecast with no terminal failure flag.
3. History failure replaces previous rows with null, the same sentinel consumed
   by `HistoryCard(rows.isNullOrEmpty())` as empty history.
4. Model publishes a filter with zero particles before its rendering code uses
   unchecked median indexing.

Additional direct source observations: Today's error card was guarded by
`failed && forecast == null`, hiding refresh failures when cache existed; Drift
had no failure flag at all. Model load failure had the same null-to-Loading guard
as Drift. These rendering observations are source checks, not executed Compose UI.
The four baseline tests intentionally fail, and their expected exit code is 1.

## Evidence and reproduction

- `tools/test-load-offline.py`: compiles the actual `LoadState.kt` plus verbatim
  extracted Today, Drift, history and Model LaunchedEffect bodies. Tests use
  synthetic Engine, filter, DAO, Android context and cache boundaries. The helper
  and effect code are not reimplemented in the harness. No Compose is compiled.
- 23 deterministic tests: first/cached failure, recovery, success, cancellation
  identity, cancellation before start, swallowed downstream cancellation,
  cancellation during failure, fatal errors, Today ancillary cancellation,
  history failure versus true empty success, Model empty-particle validation,
  consistent cached Model pair, and an obsolete request completing after a new
  request. Synchronization uses coroutine deferreds; no arbitrary sleeps/network.
- `tools/test-load-wiring.py`: static checks of error-before-spinner guards,
  retry keys/buttons, cache labels, history state separation, cached and fresh
  Model validation, resource XML/duplicates and disabled scoring protocol.
- Logs: `stage5-load.txt`, `stage5-legacy-load.txt`, `stage5-wiring.txt`.
- Independent source review of the helper, extracted-effect harness and UI diff
  found no remaining blocker within this bounded scope; platform gates remain.
- JDK 17, already-present Gradle 8.10.2 compiler/JUnit jars and coroutines 1.9.0;
  one CPU, nice 15, 384 MB JVM. Set `VESPIAN_JAVA_HOME`, `VESPIAN_GRADLE_HOME` and
  `VESPIAN_COROUTINES_JAR` to existing tool locations, then run the scripts.
- `stage5-unchanged.txt` records byte-identical earlier model/storage sources and
  harnesses. Previous suites are retained evidence, not claimed reruns here.

## Boundaries and remaining gates

This is not an Android/Compose build, lifecycle/device test, Room integration test
or real Engine prediction run. The harness executes extracted effect bodies with
ordinary Kotlin fields; it does not prove Compose scheduling, recomposition,
resource resolution or visual layout. Full Android compilation and device testing
remain release gates, including Retry, tab switching and rapid refresh.

Nested Engine/worker `runCatching` outside these tab effects is unchanged and
unaudited in this stage. The helper detects cancelled context on return but does
not make all model computation cooperatively cancellable, interrupt blocking
work, roll back completed Engine/database side effects or serialize different
active callers. Cache age/expiration beyond visible refresh status is unchanged.
Other screens and Today ancillary ordinary-error messaging are outside scope.
History remains below the forecast section and is not rendered while an initial
forecast is unavailable. Its independent load still runs; forecast retry does not
silently retry or erase history errors.

Zero-night prediction presentation remains unchanged for a separate stage.
Forecast evaluation stays disabled; predictive/clinical usefulness is unproven.
No model/scoring change, permission, dependency, backup policy, secret handling,
remote action or data migration was introduced. Stage 4 remains untouched for
source rollback; installed-app downgrade safety is not asserted. GPL/MIT notices
and existing public test-only signing fixture are unchanged.
