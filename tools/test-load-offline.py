#!/usr/bin/env python3
"""Compile actual load helper and extracted, unchanged LaunchedEffect bodies.
Explicit fake Engine/DAO/Android boundaries; no Compose or Android runtime.
--legacy ROOT reproduces four known stage-4 failures as expected failing tests.
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
main=production/'app/src/main/java/dev/vespian/MainActivity.kt';text=main.read_text()
def effects(tab):
 section=text.split('private fun '+tab+'(',1)[1].split('\n@Composable',1)[0]
 result=[];start=0
 while True:
  pos=section.find('LaunchedEffect(',start)
  if pos<0:return result
  begin=section.index('{',pos);depth=1;end=begin+1
  while depth:
   if section[end]=='{':depth+=1
   elif section[end]=='}':depth-=1
   end+=1
  result.append(section[begin+1:end-1]);start=end
bodies={'today':effects('TodayTab')[0],'drift':effects('DriftTab')[0],'model':effects('ModelTab')[0]}
if not args.legacy:bodies['historyEffect']=effects('DriftTab')[1]
source='package dev.vespian.ui\nimport kotlinx.coroutines.*\nclass Effects : Fields() {\n'+''.join('suspend fun '+name+'() {\n'+body+'\n}\n' for name,body in bodies.items())+'}\n'
name='LegacyLoadHarness' if args.legacy else 'LoadHarness'
sources=[root/'app/src/main/java/dev/vespian/ui/LoadState.kt',root/'tools/load-tests/Boundaries.kt',root/'tools/load-tests'/f'{name}.kt']
print('EXTRACTED actual LaunchedEffect bodies from',main,'sha256='+hashlib.sha256(main.read_bytes()).hexdigest(),flush=True)
for x in sources:print('SOURCE',x.name,'sha256='+hashlib.sha256(x.read_bytes()).hexdigest(),flush=True)
cp=os.pathsep.join(map(str,jars));cmd=[str(java),'-XX:ActiveProcessorCount=1','-Xmx384m']
with tempfile.TemporaryDirectory(prefix='vespian-load-tests-') as d:
 tmp=Path(d);generated=tmp/'Effects.kt';generated.write_text(source);sources.append(generated);classes=tmp/'classes'
 subprocess.run(cmd+['-cp',str(lib/'*'),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-jvm-target','17','-classpath',cp,'-d',str(classes),*map(str,sources)],check=True)
 print('Actual effect bodies + load helper; fake Engine/DAO; Compose rendering NOT compiled',flush=True)
 raise SystemExit(subprocess.run(cmd+['-cp',str(classes)+os.pathsep+cp,'org.junit.runner.JUnitCore','dev.vespian.ui.'+name]).returncode)
