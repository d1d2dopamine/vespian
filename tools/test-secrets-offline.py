#!/usr/bin/env python3
"""Test actual Secrets.kt failure control flow with explicit fake Android/crypto.
No real credentials, encryption, keystore, file IO or network are exercised.
Use --legacy ROOT to reproduce the original fallback as an expected red test.
"""
import os,sys,subprocess,tempfile,hashlib,argparse
from pathlib import Path
root=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--legacy',type=Path);args=p.parse_args()
production=args.legacy.resolve() if args.legacy else root
lib=Path(os.environ['VESPIAN_GRADLE_HOME'])/'lib';java=Path(os.environ['VESPIAN_JAVA_HOME'])/'bin/java'
jars=[lib/n for n in ['kotlin-stdlib-1.9.24.jar','junit-4.13.2.jar','hamcrest-core-1.3.jar','annotations-24.0.1.jar']]
for x in [java,lib/'kotlin-compiler-embeddable-1.9.24.jar',*jars]:
 if not x.is_file():raise SystemExit('Missing existing tool '+str(x))
if hasattr(os,'sched_getaffinity'):os.sched_setaffinity(0,{max(os.sched_getaffinity(0))})
if hasattr(os,'nice'):os.nice(15)
name='LegacySecretsProbe' if args.legacy else 'SecretsSafetyHarness'
sources=[production/'app/src/main/java/dev/vespian/tg/Secrets.kt']+[root/'tools/secrets-tests'/n for n in ['AndroidBoundary.kt','CryptoBoundary.kt',name+'.kt']]
for x in sources:print('SOURCE',x.name,'sha256='+hashlib.sha256(x.read_bytes()).hexdigest(),flush=True)
cp=os.pathsep.join(map(str,jars));cmd=[str(java),'-XX:ActiveProcessorCount=1','-Xmx384m']
with tempfile.TemporaryDirectory(prefix='vespian-secrets-tests-') as d:
 classes=Path(d)/'classes'
 subprocess.run(cmd+['-cp',str(lib/'*'),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-jvm-target','17','-classpath',cp,'-d',str(classes),*map(str,sources)],check=True)
 print('Actual Secrets source; synthetic Android and encrypted-store failure boundary',flush=True)
 raise SystemExit(subprocess.run(cmd+['-cp',str(classes)+os.pathsep+cp,'org.junit.runner.JUnitCore','dev.vespian.tg.'+name]).returncode)
