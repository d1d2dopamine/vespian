# Stage 2: prospective timestamp integrity

Experimental source snapshot. No APK or real-night pilot.

## Implemented

- `SyncWorker` uses the actual local hour: recording is allowed from 18:00 inclusive until 04:00 exclusive. The journal checks again. The frozen output is timestamped after computation, rather than backdated to worker start.
- `Forecast` carries the offset of its numerical time axis and computation timestamp; `Engine` fills them. The journal saves UTC bounds, computation/recording times, saved zone, cycle bounds, model version and separate protocol version.
- `pred_log_v2` is separate from legacy `pred_log`. The latter is never rewritten or scored by this stage. Unknown/malformed provenance is rejected, first promise per cycle is retained under a mutex, and future inputs/clock rollback are conservatively excluded.
- The candidate matcher excludes post-onset, future and unfinished observations; works on absolute instants across travel; uses saved civil-time bounds across 23/25-hour DST cycles; and does not select a more convenient target based on the predicted time.
- Current-model filtering is explicit, and **PROTOCOL_APPROVED remains false**. Both history association and calibration exclude provisional scores. The app may still show its unvalidated width-derived confidence fallback; this is not a probability validation.

## Protocol decision deliberately left open

The source establishes an evening recording window and a >=120-minute definition of a long sleep. It does not establish the proposed evaluation cycle “first long sleep starting between 18:00 and the next 18:00”. That rule is candidate protocol 1, not an accepted scientific outcome. It requires owner agreement before actual validation.

Further blockers before enabling scoring:

- `Night.dateKey` is the wake-date primary key and the DAO uses REPLACE. Multiple long sleeps ending on one date can overwrite each other, so the stored history cannot guarantee which was first. A complete input-list matcher cannot repair missing source observations.
- Guessed/manual onset records need an explicit eligibility policy; they are not automatically equivalent to independently measured onset.
- `latestSleepEnd` is a conservative database check made while recording, not a complete frozen snapshot of every input used by the model. This stage does not establish a leakage-free retrospective backtest.

Do not merely flip the flag and claim these decisions are solved. Preserve complete episodes or exclude ambiguity, agree the target/source policy, version the protocol, then validate prospectively with explicit permission.

## Checks

- 65 core/numerical/time-policy tests pass (39 from stage 1 and 26 time-policy tests).
- 9 journal tests compile actual `PredLog`, `Calib`, `Model`, `ForecastTiming` and entity DTOs with real org.json and coroutines. A fake in-memory DAO, Room annotations, Context and small Engine boundary replace Android/Room infrastructure. These tests cover JSON roundtrip, legacy isolation, first-write retention, daytime/provenance rejection and the disabled-score guarantee. They are not a Room migration or device test.
- Three regression probes compile exact legacy Kotlin expressions from the previous source in small wrappers. All three correctly fail the intended assertions: midday is accepted, a post-onset record matches, and a sleep after 04:00 loses its preceding-evening record. This is not full legacy worker execution and is not the same suite as the 74 passing current tests.
- Read-only independent review found no source blocker for this evaluation-disabled stage. Storage/source issues remain open.

## Reproduction

Use already installed JDK17 and Gradle8.10.2; the isolated compiler is Kotlin1.9.24. Full Android compilation with the project toolchain is still required.

```sh
export VESPIAN_JAVA_HOME=/path/to/jdk17
export VESPIAN_GRADLE_HOME=/path/to/gradle-8.10.2
python3 tools/test-core-offline.py

# Existing Maven jars, not downloaded by the harness:
export VESPIAN_JSON_JAR=/path/to/json-20231013.jar
export VESPIAN_COROUTINES_JAR=/path/to/kotlinx-coroutines-core-jvm-1.9.0.jar
python3 tools/test-journal-offline.py

# Expected exit 1 and three failed assertions against the pre-stage2 source:
python3 tools/reproduce-legacy-timing.py /path/to/vespian-stage1/vespian-main
```

The stage-1 `--production-root` mode on the expanded core suite requires the new time-policy API; use the dedicated legacy timing probe for stage-2 red evidence. No harness installs or downloads anything. Runs are low priority and restricted to one CPU where supported.

## Preservation and open work

Stage 1 numerical fixes are retained. No schema/signing change, real health data, runtime secrets, redesign or new ikna code. Existing licenses and intentionally shared development signing fixture are unchanged. The prior source ZIP remains the rollback artifact; installing/downgrading APKs was not tested.

Open: actual Android/Room build and device checks, distinct-session storage, importer freshness, drink/night attribution, backups, error UI, predictive usefulness and agreed evaluation protocol.
