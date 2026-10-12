#!/usr/bin/env python3
"""Small source-wiring and actual SQLite-query checks; not an Android/Room test."""
from pathlib import Path
import re,sqlite3,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
main=root/'app/src/main/java/dev/vespian'
worker=(main/'work/SyncWorker.kt').read_text()
stale=worker.split('private suspend fun checkStale',1)[1].split('companion object',1)[0]
assert 'KEY_LAST_DATA' not in stale
assert 'lastEnded(now)?.sleepEnd' in stale
assert 'HealthFreshness.warningHours(now, lastSleepEnd, lastWarned)' in stale
assert 'Meta(KEY_LAST_DATA,' not in worker
commands=(main/'tg/Commands.kt').read_text()
status=commands.split('private suspend fun status(',1)[1].split('private suspend fun ',1)[0]
assert 'KEY_LAST_DATA' not in status
assert 'lastEnded(System.currentTimeMillis())?.sleepEnd' in status
activity=(main/'MainActivity.kt').read_text()
assert '.lastSleepEnd()' not in activity
assert activity.count('lastEnded(System.currentTimeMillis())?.sleepEnd') >= 2
dao=(main/'db/Dao.kt').read_text()
m=re.search(r'@Query\("([^"]+)"\)\s*suspend fun lastEnded\(before: Long\)',dao)
assert m, 'Cannot find actual lastEnded query'
query=m.group(1)
db=sqlite3.connect(':memory:')
db.execute('CREATE TABLE nights(dateKey TEXT PRIMARY KEY, sleepEnd INTEGER)')
assert db.execute(query,{'before':1000}).fetchone() is None
db.executemany('INSERT INTO nights VALUES (?,?)',[('old',500),('future',1500),('new',900)])
assert db.execute(query,{'before':1000}).fetchone()==('new',900)
assert db.execute(query,{'before':900}).fetchone()==('new',900)
db.execute('DELETE FROM nights WHERE dateKey="new"')
assert db.execute(query,{'before':1000}).fetchone()==('old',500)
for path in (root/'app/src/main/res').glob('values*/strings.xml'):
    resources=ET.parse(path).getroot()
    strings={e.attrib['name']:e.text or '' for e in resources.findall('string')}
    assert '%1$d' in strings['nt_stale_text']
    assert 'Последний сон завершился' in strings['nt_stale_text']
    assert '%1$d' in strings['tgb_st_data']
assert 'const val PROTOCOL_APPROVED = false' in (main/'model/ForecastTiming.kt').read_text()
print('PASS: worker/status wiring; actual SQLite lastEnded query (empty/current/future/boundary); resource XML/placeholders; scoring stays disabled')
