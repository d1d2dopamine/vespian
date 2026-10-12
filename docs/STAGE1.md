# Stage 1: particle correctness repair

2026-10-12. Experimental source snapshot, not a release APK.

## Scope

- Correct systematic resampling: choose the particle that contributed the CDF interval, skip zero-mass intervals at exact boundaries, support positive unnormalised weights and preserve equal offspring weights.
- Use posterior-weighted inverse empirical CDF quantiles for all `Filter.band` callers, including debt, onset, gate, wake and reverse-alarm summaries. Drift uses the same weighted median.
- Reject empty/mismatched/nonfinite or invalid-mass inputs rather than publish a meaningless number.
- Bump `Engine.MODEL_VERSION` from 10 to 11 so its existing loading path refits derived particles. No schema change or measured-history deletion.

The quantile convention is the smallest supported value whose cumulative mass reaches the requested fraction. This changes some small-sample/even-sized summaries deliberately. It does not make the interval a calibrated real-world probability.

## Evidence

The suite contains 17 existing arithmetic/band tests and 22 new filter tests. Tests are deterministic. The companion stage report records the final before/after totals.

Representative original failures:

- Equal parent weights: expected markers `[0,1,2,3,4,5,6,7]`; got `[1,2,3,4,5,6,7,7]`.
- Weights `[0.96,0.02,0.02]` at values `[10,20,30]`: expected median `10`; got `20`.
- Weighted drift: expected `0.1`; got `0.8`.

The suite also covers a sole supported particle in every position, unequal offspring counts, tiny subnormal weights, zero random offset and CDF boundaries, zero-weight outliers, sorting/pairing values with weights, weight scaling, high-ESS unequal weights including observe-to-forecast, forecast-band delegation and invalid inputs.

## Reproduce without Android or network access

Use an existing JDK 17 and Gradle 8.10.2 distribution. The runner reuses its Kotlin 1.9.24, JUnit 4.13.2 and Hamcrest jars; it installs and downloads nothing. Paths below are placeholders for your local tool installations.

```sh
export VESPIAN_JAVA_HOME=/path/to/jdk17
export VESPIAN_GRADLE_HOME=/path/to/gradle-8.10.2
python3 tools/test-core-offline.py
# Run the identical tests against an untouched original checkout:
python3 tools/test-core-offline.py --production-root /path/to/original/vespian-main
```

This compiles the actual selected `Filter.kt` and `Model.kt`, not a rewritten algorithm. Compile-only stubs supply `Night`, `Answer`, `JSONArray`, `Calib.Score` and `Behaviour.MAX_WIDEN` (the last constant is read from the selected source). JSON methods fail if called. The tests do **not** exercise real database, JSON, calibration, behaviour-layer or Android integration. Compiler/test processes are limited to one CPU and low priority where the OS supports it. The older standalone compiler is a bounded numerical check, not a substitute for the project's Kotlin 2.0.21/Android build.

## Open risks and deliberate exclusions

No Android SDK was installed or licensed; no APK, UI, phone, migration, restore or full-app integration test was run. Real predictive usefulness remains unproven. Known prospective-logging, drink-date attribution, freshness, backup, UI-error and confidence-calibration issues remain for later bounded stages. Existing prediction logs and their calibration are not cleared or retrospectively corrected here. Do not treat old or new confidence percentages as validated probabilities.

The original archive remains the rollback source; SHA-256:
`bf903087f8b2d1b331d11741093066348d836c4e440494616036687ff1157042`.
The source ZIP preserves the original GPLv3 license and README's GPLv3-or-later notice, and the bundled trial3lib MIT license. No new ikna code/assets were imported.

The supplied `app/vespian-debug.jks` is retained unchanged as the repository's deliberately shared development/test signing fixture, as described in the original README/build script. Its identity was not rotated. It is not a production security guarantee; no new private credentials or runtime data are included. Android signing/install compatibility was not tested.
