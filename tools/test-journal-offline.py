#!/usr/bin/env python3
"""Compile actual journal, calibration, model DTOs and JSON with a fake in-memory DAO.
Needs existing JDK17/Gradle8.10.2, JSON20231013 and coroutines1.9 jars supplied by
VESPIAN_JAVA_HOME, VESPIAN_GRADLE_HOME, VESPIAN_JSON_JAR, VESPIAN_COROUTINES_JAR.
No download, Android or Room runtime. Engine is a small explicit boundary stub.
"""
import os,re,subprocess,tempfile,hashlib
from pathlib import Path
root=Path(__file__).resolve().parents[1]
lib=Path(os.environ['VESPIAN_GRADLE_HOME'])/'lib'
java=Path(os.environ['VESPIAN_JAVA_HOME'])/'bin/java'
jars=[lib/n for n in ['kotlin-stdlib-1.9.24.jar','junit-4.13.2.jar','hamcrest-core-1.3.jar','annotations-24.0.1.jar']]
jars += [Path(os.environ['VESPIAN_JSON_JAR']),Path(os.environ['VESPIAN_COROUTINES_JAR'])]
for p in [java,lib/'kotlin-compiler-embeddable-1.9.24.jar',*jars]:
 if not p.is_file():raise SystemExit('Missing existing tool: '+str(p))
if hasattr(os,'sched_getaffinity'):os.sched_setaffinity(0,{max(os.sched_getaffinity(0))})
if hasattr(os,'nice'):os.nice(15)
main=root/'app/src/main/java/dev/vespian'
sources=[main/'model'/n for n in ['Model.kt','ForecastTiming.kt','PredLog.kt','Calib.kt']]+[main/'db/Entities.kt',root/'tools/journal-tests/JournalHarness.kt']
for p in sources:print('SOURCE',p.name,'sha256='+hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
version=re.search(r'const val MODEL_VERSION = (\d+)',(main/'model/Engine.kt').read_text()).group(1)
stubs={
 'Context.kt':'package android.content\nclass Context\n',
 'Room.kt':'package androidx.room\nannotation class Entity(val tableName:String)\nannotation class PrimaryKey(val autoGenerate:Boolean=false)\n',
 'Engine.kt':'''package dev.vespian.model
object Engine {
 const val MODEL_VERSION = VERSION
 fun hourOf(at:Long):Double = at / 3_600_000.0 + java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now()).totalSeconds/3600.0
}
'''.replace('VERSION\n',version+'\n'),
 'Db.kt':'''package dev.vespian.db
import android.content.Context
class Db {
 val metaRows = linkedMapOf<String,String>()
 val nightRows = mutableListOf<Night>()
 fun meta() = MetaDao(this)
 fun nights() = NightDao(this)
 companion object { var current=Db(); fun reset(){current=Db()}; fun get(context:Context)=current }
}
class MetaDao(private val db:Db) {
 suspend fun get(key:String):String?=db.metaRows[key]
 suspend fun put(meta:Meta){db.metaRows[meta.key]=meta.value}
}
class NightDao(private val db:Db) {
 suspend fun all():List<Night> = db.nightRows.toList()
 suspend fun lastSleepEnd():Long? = db.nightRows.maxOfOrNull { it.sleepEnd }
}
'''}
cp=os.pathsep.join(map(str,jars)); cmd=[str(java),'-XX:ActiveProcessorCount=1','-Xmx384m']
with tempfile.TemporaryDirectory(prefix='vespian-journal-') as d:
 tmp=Path(d)
 for name,text in stubs.items():
  p=tmp/name;p.write_text(text);sources.append(p)
 classes=tmp/'classes'
 subprocess.run(cmd+['-cp',str(lib/'*'),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-jvm-target','17','-classpath',cp,'-d',str(classes),*map(str,sources)],check=True)
 print('Actual PredLog/Calib/ForecastTiming/JSON; FAKE DAO, Room annotations, Context and Engine boundary',flush=True)
 raise SystemExit(subprocess.run(cmd+['-cp',str(classes)+os.pathsep+cp,'org.junit.runner.JUnitCore','dev.vespian.model.JournalHarness']).returncode)
