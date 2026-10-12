# Stage 4: encrypted-storage failure and automatic-backup boundaries

Experimental source snapshot, 2026-10-12. Includes stages 1–3. No Android build,
device run, real credential, OS backup operation or clinical validation.

## Repairs and source anchors

- `app/src/main/java/dev/vespian/tg/Secrets.kt`: encrypted-only credentials.
  Keystore/open/read/editor/commit failures latch access closed. No plaintext
  fallback, legacy-file read/import/deletion or explicit key rotation. Save uses
  synchronous commit off the UI thread and reports failure. After a failed read,
  Save cannot overwrite existing values from empty fields: successful explicit
  read-only Retry is required first. Background reads do not clear the latch.
- `SettingsActivity.kt`: visible storage error and Retry; Save disabled while
  storage is unavailable. Retry reloads existing fields. Bot test runs only after
  confirmed save. Generic errors avoid displaying raw credential-bearing errors.
  `diag/SelfTest.kt` distinguishes unavailable storage from an unconfigured bot.
- `res/xml/backup_rules.xml` and `data_extraction_rules.xml`: all three backup
  sections now include only `sharedpref/vespian_ui.xml`. Database files and both
  `vespian_secrets.xml` and legacy `vespian_secrets_plain.xml` are excluded.
  The former incorrect `ikna.db` entry is removed without enabling `vespian.db`
  transfer. `Prefs.kt` has only eight ordinary settings, no tokens or measured
  health data; this is a source review, not inspection of a user's preference file.
  Android manifest and manual `Backup.kt` JSON import/export are unchanged.

Android documents that explicit includes restrict backup to selected paths and
that Android 12+ separates cloud/device-transfer rules:
https://developer.android.com/identity/data/autobackup
EncryptedSharedPreferences documentation explicitly warns against backing up
its files because the encryption key may be absent after restoration:
https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
These are configuration intentions, not proof of behavior on every device/OEM.

## Compatibility and decisions

Automatic backup coverage is narrowed to ordinary UI settings. Sleep database,
model state, logs and credentials are not enabled for automatic transfer. Manual
JSON backup remains available with its existing limitations. Existing remote
backups are not purged by this source change.

Legacy plaintext credentials remain untouched on a device but are no longer
used. A user relying on that fallback may need to configure Telegram explicitly
again once encrypted storage works. No automatic migration/deletion is included.
Persistent Keystore loss is not guaranteed recoverable with Retry. Decide an
explicit migration/reset policy before addressing that case; do not silently
clear storage or rotate keys. Decide separately whether sleep database transfer
should ever be enabled and how consent, restore and confidentiality should work.

## Evidence and reproduction

- `tools/test-secrets-offline.py`: **14 tests PASS**, compiling actual Secrets.kt
  against explicit synthetic Android/crypto/preferences boundaries. Covers read,
  create, key, edit and commit failures; failure latching; read-only retry;
  prevention of blank overwrite after recovery; no plaintext migration/deletion.
- `--legacy /path/to/stage3/vespian-main`: **1 expected assertion failure**.
  The unchanged previous Secrets opens plaintext preferences after injected
  encrypted-provider failure. This reproduces source control flow, not actual
  Android cryptography.
- `tools/test-backup-secret-wiring.py`: PASS. XML fixture allowlist, known UI-pref
  keys and writer, source recovery/save ordering, manifest and resource wiring.
- JDK 17 + already-present Gradle 8.10.2 Kotlin 1.9.24/JUnit jars; one CPU, 384 MB
  JVM, nice 15, no downloads. Set `VESPIAN_JAVA_HOME` and `VESPIAN_GRADLE_HOME`.
- Logs: `stage4-secrets.txt`, `stage4-legacy-secrets.txt`, `stage4-wiring.txt`.
- Read-only independent review found the blank-overwrite case; it was fixed and
  regression-tested, then reviewed again without a remaining bounded blocker.
- Earlier 78 core tests and 9 journal tests were NOT rerun here. Their tested
  sources/harnesses are byte-identical to stage 3; retained logs and explicit
  comparison are in `stage4-unchanged.txt`.

No Compose compilation, Android Keystore/filesystem, Room runtime, OS restore,
network bot test or encryption-strength validation. The app's actual Kotlin/AGP
build remains a platform gate. Forecast evaluation stays disabled and predictive
utility remains unproven. This stage changes no numerical model or visual design.

Original and stage-3 archives remain rollback source snapshots; this is not a
promise of safe installed-app downgrade. GPL and bundled MIT notices unchanged.
The original public development signing fixture is retained unchanged, test-only;
it offers no production signing security. No new credentials or user data added.
