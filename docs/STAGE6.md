# Stage 6: zero-night forecast presentation floor

Experimental source snapshot, 2026-10-12. Includes stages 1–5. No Android SDK,
platform build, device run, downloads, live requests, credentials or user data.

## Bounded change

`ForecastPresentation.canPresent(nights)` is a presentation-only test of `nights > 0`.
It blocks zero and negative counts. One recorded night permits the old output;
this is **not** proof of forecast accuracy, calibration, safety, clinical validity
or enough data. No new multi-night threshold or validation claim is introduced.

- Today replaces the ring, times, confidence, forecast-based advice, explanation,
  habit estimate and reverse-alarm forecast with an unavailable card. `LogCard`
  remains directly accessible in this cold branch, including its existing
  mode-dependent controls. The existing SectionCard style is retained.
- Drift hides its drift number, projection chart and week times. Historical
  records and their independent loading/error state remain outside the guards.
- Widget build uses the existing unavailable label and explicitly clears both
  range and gate text. No widget lifecycle changes were made.
- `Commands.forecastText` returns null, so the manual forecast command uses its
  existing unavailable reply. `why` likewise replies unavailable before producing
  times, confidence or explanations. `corr` already rejected counts below one.
- `Bot.maybeEvening` returns before forecast timing, enqueue and the sent-day
  marker. Its existing second forecastText lookup independently refuses zero
  nights, including if that count changes between lookups. No cold-start
  automated forecast or automated unavailable reply is queued by this path.
- Model diagnostics remain visible with a warning that parameters may include
  starting assumptions (priors) or a saved calculation. They are not an established
  personal forecast or sleep measurements. Zero nights does not prove that every
  parameter is a pristine prior: saved model state and auxiliary inputs can exist.
- Both existing resource variants receive matching Russian cold-start and priors
  labels, consistent with the source snapshot's existing resource languages.

## Consumer audit and unchanged behavior

All `Engine.forecast` consumers were inspected: Today, Drift, the widget,
Commands forecast/why/corr, Bot evening and SyncWorker prediction recording.
The last delegates to `PredLog.record`, whose nonpositive-night rejection was
already present and remains unchanged. Ring/chart components receive forecast
values only behind the corresponding UI guards. Notify/Nudge do not format
forecast times. Historical `PredLog` displays, manual data entry and manual
backup/export are unchanged.

Engine still uses `db.nights().count()` without a new eligibility filter. Count
semantics, model calculations, calibrated output, Engine cache keys/age and
retained UI forecast caches are unchanged. Therefore this enforces the count on
the forecast currently being presented; it does not establish that its nights
are recent, valid or observed, and does not invalidate a positive cached forecast
when underlying storage changes. Existing refresh/stale labels remain in effect.
Model diagnostics use their existing loaded count pair. This is a zero-count
presentation boundary, not a data-quality or cache-consistency repair.

`ForecastTiming.PROTOCOL_APPROVED` stays false. No scoring activation, migration,
dependency, permission, secret handling, backup policy or model change. Stage 5
is preserved separately for source rollback; installed-app downgrade safety is
not asserted. Existing licence notices and public test fixture are unchanged.

## Evidence and reproduction

`tools/test-cold-offline.py` compiles the actual pure presentation helper and
verbatim extracted full bodies of forecastText, why, maybeEvening and widget
build. Explicit synthetic Engine/model, Context/resources, RemoteViews, outbox
and metadata boundaries replace external dependencies. Tests cover nonpositive
counts, positive-count compatibility, ordinary forecast failure, and the
positive-to-zero evening second lookup. Helper tests include integer limits.
No real Engine forecast or send is run.

`tools/test-cold-offline.py --legacy /path/to/stage5/vespian-main` applies the same
expectations to the unchanged stage-5 bodies. The helper is supplied from stage6
but unused by legacy bodies. This demonstrates presentation defects rather than
mocking an alternate copy of production control flow.

`tools/test-cold-wiring.py` statically checks Today return/control preservation,
Drift/history/week containment, priors disclosure, both XML resource variants
and retained journal/scoring guards. `tools/test-load-wiring.py` additionally
checks that stage-5 load-state wiring is intact.

Use existing JDK 17, Gradle 8.10.2 compiler/JUnit jars and cached coroutines 1.9.0:
set `VESPIAN_JAVA_HOME`, `VESPIAN_GRADLE_HOME`, `VESPIAN_COROUTINES_JAR`, then invoke
the scripts. JVM runs use one CPU, nice 15 and a 384 MB heap. No Gradle build or
network resolution is required.

Recorded results (2026-10-12):

- `stage6-cold.txt`: 8/8 actual-body/helper tests pass.
- `stage6-legacy-cold.txt`: the same 8 tests compile against stage5, with 5
  expected failures (exit 1): zero-night forecast text, why text, widget times,
  automatic evening send/day marker, and the evening positive-to-zero recheck.
  The ordinary failure path and positive-night compatibility tests pass there.
- `stage6-wiring.txt`: 19 stage6 static checks pass; existing stage5 load wiring
  checks also pass. These are not executed UI tests.
- `stage6-unchanged.txt`: byte-identical earlier sources/tests listed against
  stage5; only six pre-existing source/resource files differ. Model, storage,
  backup, scoring and earlier test harnesses are unchanged. Earlier compiled
  suites are retained evidence, not claimed reruns in this stage.

## Honest limits and release gates

Extracted-body tests are not an Android build, Compose rendering test, launcher
widget test, real resource resolution test, Room integration or model validation.
Static UI checks cannot prove recomposition, screen geometry, accessibility,
localization or lifecycle behavior. Device gates still include fresh install,
zero-night Today logging, tab switching, history visibility, widget refresh,
manual forecast/why replies and absence of automatic zero-night evening sends.
Historical already-queued messages are not purged or changed by this stage.

One-night outputs and scientific usefulness remain unvalidated. Full Android
compilation and device tests remain open; this snapshot is not a release-ready
APK and the broader beta checklist is not marked complete.
