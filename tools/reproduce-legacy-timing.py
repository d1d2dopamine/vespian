#!/usr/bin/env python3
"""Reproduce three legacy timing defects from exact Kotlin source expressions.
Pass an untouched pre-stage2 source root. This compiles small wrappers around
extracted expressions, NOT the Android worker or complete old app. A failure
of all three assertions is the expected red result. No dependencies downloaded.
"""
import os,re,sys,subprocess,tempfile,hashlib
from pathlib import Path
original=Path(sys.argv[1]).resolve(); main=original/'app/src/main/java/dev/vespian'
texts={name:(main/path).read_text() for name,path in {'engine':'model/Engine.kt','worker':'work/SyncWorker.kt','log':'model/PredLog.kt','commands':'tg/Commands.kt'}.items()}
for name,text in texts.items():print('ORIGINAL',name,'sha256='+hashlib.sha256(text.encode()).hexdigest(),flush=True)
def extract(pattern,text):
 m=re.search(pattern,text,re.S)
 if not m:raise SystemExit('Legacy source shape not recognised; refusing to fabricate a baseline')
 return m.group(1)
hour=extract(r'fun hourOf\(epochMillis: Long, offset: Double = offsetHours\(\)\): Double =\s*([^\n]+)',texts['engine'])
gate=extract(r'val hour = Engine.hourOf\(now\)\s*if \(([^\n]+)\) return',texts['worker'])
start=extract(r'const val EVENING_FROM_H = ([0-9.]+)',texts['worker'])
end=extract(r'const val EVENING_TO_H = ([0-9.]+)',texts['worker'])
day=extract(r'internal fun dayKey\(at: Long = System.currentTimeMillis\(\)\): String =\s*(.*?\.toString\(\))',texts['commands'])
daystart=extract(r'const val DAY_START_H = ([0-9]+L?)',texts['commands'])
row=extract(r'(Row\(dateKey = key, night = night, pred = preds\[key\]\))',texts['log'])
code='''import java.time.Instant
import java.time.ZoneId
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test
object Commands { const val DAY_START_H = __DAYSTART__; fun dayKey(at:Long):String = __DAYEXPR__ }
data class Entry(val at:Long, val dateKey:String)
data class Night(val sleepStart:Long)
data class Row(val dateKey:String,val night:Night,val pred:Entry?)
fun hourOf(epochMillis:Long,offset:Double):Double = __HOUREXPR__
fun gate(now:Long):Boolean { val EVENING_FROM_H=__START__; val EVENING_TO_H=__END__; val hour=hourOf(now,0.0); return !(__GATEEXPR__) }
fun match(entry:Entry,night:Night):Row { val preds=listOf(entry).associateBy {it.dateKey};val key=Commands.dayKey(night.sleepStart);return __ROWEXPR__ }
class LegacyTimingTest {
 init {TimeZone.setDefault(TimeZone.getTimeZone("UTC"))}
 private fun t(s:String)=Instant.parse(s).toEpochMilli()
 @Test fun daytime_must_be_blocked(){ assertFalse(gate(t("2026-10-12T12:00:00Z"))) }
 @Test fun post_onset_record_must_be_ungraded(){ val at=t("2026-10-13T03:00:00Z");assertNull(match(Entry(at,Commands.dayKey(at)),Night(t("2026-10-13T02:00:00Z"))).pred) }
 @Test fun crossing_04_must_not_lose_the_preceding_evening(){ val at=t("2026-10-12T18:00:00Z");assertNotNull(match(Entry(at,Commands.dayKey(at)),Night(t("2026-10-13T06:00:00Z"))).pred) }
}
'''
for key,value in {'DAYSTART':daystart,'DAYEXPR':day,'HOUREXPR':hour,'GATEEXPR':gate,'START':start,'END':end,'ROWEXPR':row}.items():code=code.replace("__"+key+"__",value)
lib=Path(os.environ['VESPIAN_GRADLE_HOME'])/'lib';java=Path(os.environ['VESPIAN_JAVA_HOME'])/'bin/java'
jars=[lib/n for n in ['kotlin-stdlib-1.9.24.jar','junit-4.13.2.jar','hamcrest-core-1.3.jar','annotations-24.0.1.jar']]
for p in [java,*jars]:
 if not p.is_file():raise SystemExit('Missing existing tool '+str(p))
if hasattr(os,'sched_getaffinity'):os.sched_setaffinity(0,{max(os.sched_getaffinity(0))})
if hasattr(os,'nice'):os.nice(15)
cp=os.pathsep.join(map(str,jars));cmd=[str(java),'-XX:ActiveProcessorCount=1','-Xmx384m']
with tempfile.TemporaryDirectory(prefix='vespian-legacy-time-') as d:
 tmp=Path(d);source=tmp/'Legacy.kt';source.write_text(code);classes=tmp/'classes'
 subprocess.run(cmd+['-cp',str(lib/'*'),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-jvm-target','17','-classpath',cp,'-d',str(classes),str(source)],check=True)
 print('Expected RED: extracted legacy expressions, not full worker/Android execution',flush=True)
 raise SystemExit(subprocess.run(cmd+['-cp',str(classes)+os.pathsep+cp,'org.junit.runner.JUnitCore','LegacyTimingTest']).returncode)
