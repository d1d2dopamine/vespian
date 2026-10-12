#!/usr/bin/env python3
"""Source/XML policy checks, not Android backup/restore or keystore execution."""
from pathlib import Path
import re,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
res=root/'app/src/main/res'
legacy=ET.parse(res/'xml/backup_rules.xml').getroot()
modern=ET.parse(res/'xml/data_extraction_rules.xml').getroot()
containers=[legacy,modern.find('cloud-backup'),modern.find('device-transfer')]
for container in containers:
    assert container is not None
    includes={(e.get('domain'),e.get('path')) for e in container.findall('include')}
    excludes={(e.get('domain'),e.get('path')) for e in container.findall('exclude')}
    assert includes=={('sharedpref','vespian_ui.xml')}
    assert {('database','.'),('sharedpref','vespian_secrets.xml'),('sharedpref','vespian_secrets_plain.xml')} <= excludes
    assert 'ikna.db' not in ET.tostring(container).decode()
    # Simulate only the documented exact-file allowlist semantics, not an OS transport.
    for domain,path,expected in [('sharedpref','vespian_ui.xml',True),('sharedpref','vespian_secrets.xml',False),
       ('sharedpref','vespian_secrets_plain.xml',False),('sharedpref','future_credentials.xml',False),
       ('database','vespian.db',False),('database','vespian.db-wal',False),('file','export.json',False)]:
        included=(domain,path) in includes
        excluded=(domain,path) in excludes or (domain,'.') in excludes
        assert (included and not excluded)==expected
main=root/'app/src/main/java/dev/vespian'
prefs=(main/'Prefs.kt').read_text()
keys=set(re.findall(r'private const val KEY_[A-Z_]+ = "([a-z_]+)"',prefs))
assert keys=={'onboarded','bot_lang','manual_mode','mg_per_mug','mg_per_can','drink_energy','drink_alcohol','ask_drinks'},keys
assert 'private const val FILE = "vespian_ui"' in prefs
writers=[p for p in main.rglob('*.kt') if '"vespian_ui"' in p.read_text()]
assert writers==[main/'Prefs.kt'],writers
secrets=(main/'tg/Secrets.kt').read_text()
assert 'getSharedPreferences' not in secrets
assert '_plain' not in secrets
assert '.commit()' in secrets and '.apply()' not in secrets
assert 'delete' not in re.sub(r'/\*.*?\*/|//[^\n]*','',secrets,flags=re.S)
settings=(main/'SettingsActivity.kt').read_text()
assert 'withContext(Dispatchers.IO) { Secrets.save(context, token, chatId) }' in settings
assert 'tgResult = if (saved)' in settings
assert 'Secrets.retry(context)' in settings
assert 'enabled = !tgBusy && !tgStorageUnavailable' in settings
assert 'if (storageUnavailable) return@synchronized false' in secrets
assert 'if (pair(context) == null) return@synchronized false' in secrets
assert 'getOrElse { it.message }' not in settings.split('Section(stringResource(R.string.settings_telegram))',1)[1].split('Section(',1)[0]
for path in res.glob('values*/strings.xml'):
    strings={x.get('name'):x.text for x in ET.parse(path).getroot().findall('string')}
    for name in ['tg_storage_unavailable','tg_storage_retry','tg_test_failed','tg_secure_storage_hint']:assert strings.get(name)
manifest=ET.parse(root/'app/src/main/AndroidManifest.xml').getroot().find('application')
ns='{http://schemas.android.com/apk/res/android}'
assert manifest.get(ns+'fullBackupContent')=='@xml/backup_rules'
assert manifest.get(ns+'dataExtractionRules')=='@xml/data_extraction_rules'
print('PASS: three backup rule sets and fixture allowlist; known UI-pref keys/writer; no plaintext/delete path; encrypted commit and UI recovery wiring; resources/manifest')
