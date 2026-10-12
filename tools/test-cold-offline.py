#!/usr/bin/env python3
"""Actual unchanged method bodies + real presentation helper, fake external boundaries.
No Android/Compose compilation, network, model calculation, credentials or user data.
--legacy ROOT runs the same expectations against stage5 (expected failures).
"""
import os, subprocess, tempfile, hashlib, argparse, re
from pathlib import Path
root=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--legacy',type=Path);args=p.parse_args()
production=args.legacy.resolve() if args.legacy else root
base=production/'app/src/main/java/dev/vespian'
def body(file, marker):
 s=(base/file).read_text(); pos=s.index(marker); begin=s.index('{',pos); depth=1;end=begin+1
 while depth:
  if s[end]=='{':depth+=1
  elif s[end]=='}':depth-=1
  end+=1
 print('EXTRACTED',file,marker,'sha256='+hashlib.sha256(s.encode()).hexdigest(),flush=True)
 return s[begin+1:end-1]
bodies={
 'forecastText':body('tg/Commands.kt','internal suspend fun forecastText('),
 'why':body('tg/Commands.kt','private suspend fun why('),
 'maybeEvening':body('tg/Bot.kt','private suspend fun maybeEvening('),
 'build':body('work/ForecastWidget.kt','private suspend fun build('),
}
methods='''package probe
import dev.vespian.model.ForecastPresentation
import java.time.LocalDate
object Commands {
 suspend fun forecastText(context: Context): String? { %s }
 suspend fun why(context: Context) { %s }
 fun menu(context: Context): Any? = null
}
object Bot {
 const val K_EVENING = "evening"
 suspend fun maybeEvening(context: Context) { %s }
}
object Widget {
 suspend fun build(context: Context): RemoteViews { %s }
}
'''%tuple(bodies.values())
# Stage7 splits view construction/fallback out of build; retain actual helper bodies.
widget_source=(base/'work/ForecastWidget.kt').read_text()
widget_helpers=''
for name, signature in [('createViews', 'context: Context'), ('unavailable', 'context: Context, views: RemoteViews')]:
 if 'private fun '+name+'(' in widget_source:
  widget_helpers += ' fun '+name+'('+signature+'): RemoteViews { '+body('work/ForecastWidget.kt','private fun '+name+'(')+' }\n'
methods=methods.replace('object Widget {', 'object Widget {\n'+widget_helpers)
names=sorted(set(re.findall(r'R\.string\.(\w+)',methods)))
resources='package probe\nobject R { object string {\n'+''.join(f' const val {n} = "{n}"\n' for n in names)+'}\nobject layout { const val widget_forecast = 1 }; object id { const val widget_root=0; const val widget_value=1; const val widget_range=2; const val widget_gate=3 } }\n'
lib=Path(os.environ['VESPIAN_GRADLE_HOME'])/'lib';java=Path(os.environ['VESPIAN_JAVA_HOME'])/'bin/java'
jars=[lib/n for n in ['kotlin-stdlib-1.9.24.jar','junit-4.13.2.jar','hamcrest-core-1.3.jar','annotations-24.0.1.jar']]+[Path(os.environ['VESPIAN_COROUTINES_JAR'])]
for x in [java,*jars]:
 if not x.is_file():raise SystemExit('Missing existing tool '+str(x))
if hasattr(os,'sched_getaffinity'):os.sched_setaffinity(0,{max(os.sched_getaffinity(0))})
if hasattr(os,'nice'):os.nice(15)
cp=os.pathsep.join(map(str,jars));cmd=[str(java),'-XX:ActiveProcessorCount=1','-Xmx384m']
with tempfile.TemporaryDirectory(prefix='vespian-cold-tests-') as d:
 tmp=Path(d);a=tmp/'Bodies.kt';a.write_text(methods);b=tmp/'Resources.kt';b.write_text(resources);classes=tmp/'classes'
 sources=[root/'app/src/main/java/dev/vespian/model/ForecastPresentation.kt',a,b,*sorted((root/'tools/cold-tests').glob('*.kt'))]
 subprocess.run(cmd+['-cp',str(lib/'*'),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-jvm-target','17','-classpath',cp,'-d',str(classes),*map(str,sources)],check=True)
 print('Actual command/why/evening/widget bodies, fake Engine/Android/outbox; no Compose or Android runtime',flush=True)
 raise SystemExit(subprocess.run(cmd+['-cp',str(classes)+os.pathsep+cp,'org.junit.runner.JUnitCore','probe.ColdHarness']).returncode)
