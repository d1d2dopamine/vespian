#!/usr/bin/env python3
"""Source/resource contracts for load-state UI; no Compose runtime claims."""
from pathlib import Path
import re, xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
main=root/'app/src/main/java/dev/vespian'
s=(main/'MainActivity.kt').read_text()
def tab(name): return s.split('private fun '+name+'(',1)[1].split('\n@Composable',1)[0]
for name,state in [('TodayTab','forecastLoad'),('DriftTab','forecastLoad'),('ModelTab','modelLoad')]:
 t=tab(name)
 effect=t.split('LaunchedEffect(',1)[1].split('    LoadNotice(',1)[0]
 assert 'runCatching' not in effect, name
 assert 'refresh, retry' in effect, name
 assert state+' = '+state+'.copy(loading = true, failed = false)' in effect, name
 assert 'if ('+state+'.failed) return\n        Loading()' in t, name
 assert 'LoadNotice('+state in t and '{ retry++ }' in t, name
notice=s.split('private fun <T> LoadNotice(',1)[1].split('// ---- today',1)[0]
assert 'if (state.failed)' in notice
assert 'state.loading && state.value != null' in notice
assert 'TextButton(onClick = onRetry)' in notice
assert 'if (state.value == null) errorRes else staleRes' in notice
history=tab('DriftTab')
assert 'LaunchedEffect(refresh, historyRetry)' in history
assert 'if (!historyLoad.failed) UiCache.history = historyLoad.value' in history
assert 'historyLoad.value?.let { HistoryCard(it) }' in history
assert 'if (historyLoad.loading && historyLoad.value == null)' in history
assert 'historyRetry++' in history
assert 'private fun HistoryCard(rows: List<PredLog.Row>)' in s
model=tab('ModelTab')
assert 'UiCache.filter?.takeIf { it.particles.isNotEmpty() }' in model
assert model.index('require(filter.particles.isNotEmpty())') < model.index('filter to Db.get(context).nights().count()')
assert 'if (!modelLoad.failed) modelLoad.value?.let {' in model
ids=set(re.findall(r'R.string.(f_(?:refresh_error|refreshing|retry|history_\w+|model_(?:error|stale|loading)))',s))
assert len(ids)==9,ids
for path in (root/'app/src/main/res').glob('values*/strings.xml'):
 resources=ET.parse(path).getroot();names=[e.attrib['name'] for e in resources.findall('string')]
 assert len(names)==len(set(names)),path
 strings={e.attrib['name']:e.text or '' for e in resources.findall('string')}
 assert all(strings.get(key) for key in ids),path
assert 'const val PROTOCOL_APPROVED = false' in (main/'model/ForecastTiming.kt').read_text()
print('PASS: Today/Drift/Model terminal-error and retry wiring; cached refresh warnings; history loading/error/empty separation; model nonempty validation; both XML resources; scoring disabled')
