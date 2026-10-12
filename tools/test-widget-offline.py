#!/usr/bin/env python3
"""Compile unchanged widget launch body and helpers against explicit fake boundaries.
--legacy ROOT runs regression assertions against the old source (expected failures).
No Android SDK, actual broadcasts, launcher, credentials, or health data.
"""
import os, subprocess, tempfile, hashlib, argparse
from pathlib import Path
root=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--legacy',type=Path);args=p.parse_args()
production=args.legacy.resolve() if args.legacy else root
lib=Path(os.environ['VESPIAN_GRADLE_HOME'])/'lib';java=Path(os.environ['VESPIAN_JAVA_HOME'])/'bin/java'
jars=[lib/n for n in ['kotlin-stdlib-1.9.24.jar','junit-4.13.2.jar','hamcrest-core-1.3.jar','annotations-24.0.1.jar']]+[Path(os.environ['VESPIAN_COROUTINES_JAR'])]
for x in [java,lib/'kotlin-compiler-embeddable-1.9.24.jar',*jars]:
 if not x.is_file():raise SystemExit('Missing existing tool '+str(x))
if hasattr(os,'sched_getaffinity'):os.sched_setaffinity(0,{max(os.sched_getaffinity(0))})
if hasattr(os,'nice'):os.nice(15)
widget=production/'app/src/main/java/dev/vespian/work/ForecastWidget.kt';text=widget.read_text()
def braced(marker, include_signature=False):
 start=text.index(marker);begin=text.index('{',start);depth=1;end=begin+1
 while depth:
  if text[end]=='{':depth+=1
  elif text[end]=='}':depth-=1
  end+=1
 return text[start:end] if include_signature else text[begin+1:end-1]
body=braced('CoroutineScope(Dispatchers.Default).launch')
helpers=[braced('private suspend fun build',True)]
if not args.legacy:helpers += [braced('private fun createViews',True),braced('private fun unavailable',True)]
# hhmm is expression-bodied; retain its exact declaration up to the following KDoc.
hhmm=text.split('        private fun hhmm',1)[1].split('        /**',1)[0]
source='package dev.vespian.work\nimport dev.vespian.model.ForecastPresentation\nimport kotlinx.coroutines.*\nimport java.text.SimpleDateFormat\nimport java.util.Date\nimport java.util.Locale\nclass WidgetBody {\nsuspend fun render(context: Context, manager: AppWidgetManager, ids: IntArray, pending: Pending) {\n'+body+'\n}\n'+'\n'.join(helpers)+'\nprivate fun hhmm'+hhmm+'\n}\n'
sources=[root/'app/src/main/java/dev/vespian/model/ForecastPresentation.kt',root/'tools/widget-tests/Boundaries.kt',root/'tools/widget-tests/WidgetHarness.kt']
print('EXTRACTED actual widget launch body and helpers from',widget,'sha256='+hashlib.sha256(widget.read_bytes()).hexdigest(),flush=True)
for x in sources:print('SOURCE',x.name,'sha256='+hashlib.sha256(x.read_bytes()).hexdigest(),flush=True)
cp=os.pathsep.join(map(str,jars));cmd=[str(java),'-XX:ActiveProcessorCount=1','-Xmx384m']
with tempfile.TemporaryDirectory(prefix='vespian-widget-tests-') as d:
 tmp=Path(d);generated=tmp/'WidgetBody.kt';generated.write_text(source);sources.append(generated);classes=tmp/'classes'
 subprocess.run(cmd+['-cp',str(lib/'*'),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-jvm-target','17','-classpath',cp,'-d',str(classes),*map(str,sources)],check=True)
 print('Actual launch body/build/helpers; fake Android and Engine; no provider dispatch or Android runtime',flush=True)
 raise SystemExit(subprocess.run(cmd+['-cp',str(classes)+os.pathsep+cp,'org.junit.runner.JUnitCore','dev.vespian.work.WidgetHarness']).returncode)
