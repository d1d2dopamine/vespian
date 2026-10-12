#!/usr/bin/env python3
"""Static presentation wiring assertions; these do not render or compile Android UI."""
from pathlib import Path
import xml.etree.ElementTree as ET
r=Path(__file__).resolve().parents[1];j=r/'app/src/main/java/dev/vespian';s=(j/'MainActivity.kt').read_text()
def section(name):return s.split('private fun '+name+'(',1)[1].split('\n@Composable',1)[0]
def block(text,start):
 b=text.index('{',start);depth=1;e=b+1
 while depth:
  depth+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[b+1:e-1],e
checks=[]
def ok(label,condition):
 assert condition,label
 checks.append(label)
today=section('TodayTab');gate=today.index('if (!ForecastPresentation.canPresent(f.nights))');cold,end=block(today,gate)
ok('Today guard precedes every forecast visual',end<today.index('DayRing(') and end<today.index('val toGate'))
ok('Today cold branch retains input controls and returns', 'ColdForecastCard()' in cold and 'LogCard(refresh, onChanged)' in cold and cold.rstrip().endswith('return'))
for visual in ['BandRow(', 'WhyCard(', 'TrackRecordCard(', 'Delay.applied(', 'val reverse = f.reverseAlarm']:
 ok('Today '+visual+' is after cold return',today.index(visual)>end)
drift=section('DriftTab');guard=drift.index('if (!ForecastPresentation.canPresent(f.nights))');cold,end=block(drift,guard);forward,after=block(drift,drift.index('else {',end))
ok('Drift cold branch uses unavailable card','ColdForecastCard()' in cold)
ok('Drift value and chart inside positive else',all(v in forward for v in ['f.driftPerDay','DriftChart(']))
history=drift.index('historyLoad.value?.let { HistoryCard(it) }');week=drift.index('if (ForecastPresentation.canPresent(f.nights))');weekbody,end=block(drift,week)
ok('Historical records outside forecast guards',after<history<week)
ok('Week times inside positive guard','R.string.f_week' in weekbody and 'dayTime(Engine.millisOf(shifted))' in weekbody)
model=section('ModelTab');cold,end=block(model,model.index('if (!ForecastPresentation.canPresent(nights))'))
ok('Model priors disclosed before diagnostics','R.string.f_model_priors' in cold and end<model.index('Histogram('))
cmd=(j/'tg/Commands.kt').read_text();corr=cmd.split('private suspend fun corr(',1)[1].split('private fun moodName',1)[0]
ok('Existing correction guard preserved','debt == null || f.nights < 1' in corr)
ok('Existing journal floor preserved','forecast.nights <= 0' in (j/'model/PredLog.kt').read_text())
ok('Scoring remains disabled','PROTOCOL_APPROVED = false' in (j/'model/ForecastTiming.kt').read_text())
for folder in ['values','values-ru']:
 rows={x.attrib['name']:x.text for x in ET.parse(r/f'app/src/main/res/{folder}/strings.xml').getroot() if x.tag=='string'}
 ok(folder+' explicit no-forecast wording','прогноз недоступен' in rows['f_cold'])
 ok(folder+' model parameters called priors','приоры' in rows['f_model_priors'])
print('\n'.join('PASS '+c for c in checks));print(f'{len(checks)} static checks passed; Android/Compose NOT compiled or rendered')
