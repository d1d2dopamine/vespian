# Stage 3: measurement freshness

## Corrected

The importer rereads a five-day window and currently increments `added` for repeated nights. The former worker wrote the current download time to `last_data_at` and used that value for stale warnings. Thus an unchanged 40-hour-old night could repeatedly look zero hours old.

Freshness now uses the completion time of the latest finished sleep (`NightDao.lastEnded(now)`), not download time. The worker no longer updates or consumes the legacy download timestamp. Existing metadata is retained. Telegram `/status` and the relevant app fields use the same existing query. Future-dated rows do not hide an older valid observation.

`HealthFreshness` preserves the 30-hour warning threshold and 24-hour throttle. Future throttle timestamps after clock rollback do not suppress warnings forever. No measurements means unknown age, not evidence of a stalled feed. Wording states the age of the last sleep without claiming to know why data is old.

## Tests and limits

- 78 Kotlin/JUnit core tests pass: previous 65 plus 13 freshness tests. Cases include repeated reads, exact 30/24-hour boundaries, new measurements, missing/future values and clock rollback.
- `tools/test-freshness-wiring.py` checks worker/status/app wiring and executes the actual extracted `lastEnded` SQL on a temporary SQLite table. It covers empty/current/future/boundary selection, parses resource XML and checks placeholders, and verifies protocol scoring remains disabled.
- Source review is separate from runtime testing. No Android build, actual Room database, Health Connect sync, notification delivery or phone behavior is tested here.
- Stage-2 journal tests and evidence are retained; those unchanged journal sources were not rerun in stage 3.

```sh
export VESPIAN_JAVA_HOME=/path/to/existing/jdk17
export VESPIAN_GRADLE_HOME=/path/to/existing/gradle-8.10.2
python3 tools/test-core-offline.py
python3 tools/test-freshness-wiring.py
```

No installs/downloads, SDK terms, real health data or external messages are involved. Tests reuse the existing isolated Kotlin1.9.24/JUnit toolchain on one CPU at low priority.

## Deliberately unchanged

The inaccurate `added` counter and repeated-refit trigger remain. Simply removing refits on repeated nights could miss late heart-rate/light corrections; that ingestion change needs its own coverage. No model, schema, episode-identity, backup or UI-design change is included.

The stage-2 prospective-evaluation protocol remains unapproved and disabled. Existing source limitations (same-wake-date episode overwrite, guessed/manual observations, incomplete input provenance), predictive validation and device checks are still open.

Licenses and the supplied public-development signing fixture are unchanged. Stage 2 is the source rollback artifact; APK upgrade/downgrade safety was not tested. Current archive hashes are in `STAGE3-CHECKSUMS.sha256`; earlier manifests under docs describe the respective earlier snapshots.
