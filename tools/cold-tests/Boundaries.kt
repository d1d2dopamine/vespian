package probe

// Deliberately fake platform/model/storage/send boundaries; synthetic data only.
class Context {
 val packageName = "probe"
 fun getString(id: String, vararg args: Any): String = id + args.joinToString(prefix="(",postfix=")")
}
object Lang { fun string(context: Context, id: String, vararg args: Any) = context.getString(id,*args) }
data class Band(val median: Double=22.0,val low: Double=21.0,val high: Double=23.0) {
 val width get() = high-low
 val confidence get() = 0.6
}
class Score { fun confidence(band: Band) = 0.6 }
data class Forecast(val nights: Int, val gate: Band=Band(), val onset: Band=Band(), val wake: Band=Band(30.0), val reverseAlarm: Band?=Band(20.0),val driftPerDay: Double=0.1,val caffeineNow: Double=0.0,val calib: Score?=null)
class Particle(val latency: Double=25.0)
class Filter { val particles = listOf(Particle()) }
object Engine {
 var value: Forecast?=null
 var throwForecast=false
 var calls=0
 var onCall: ((Int)->Unit)?=null
 suspend fun forecast(context: Context): Forecast { calls++;onCall?.invoke(calls);if(throwForecast) error("fake failure");return value ?: error("fake missing") }
 fun load(context: Context)=Filter()
 fun obsStats(context: Context)=intArrayOf(0,0,0,0)
 fun offsetHours()=0.0
 fun hourOf(now: Long, offset: Double)=21.0
}
object Behaviour { const val MIN_NIGHTS=7; fun now(context: Context): Pair<Int,Int>?=null }
fun hhmm(hour: Double)="TIME[$hour]"
val sent=mutableListOf<String>()
fun say(context: Context,text: String) { sent+=text }
fun enqueue(context: Context,text: String, keyboard: Any?,menu: Any?) { sent+=text }
data class Meta(val key: String,val value: String)
class MetaDao { val rows=mutableMapOf<String,String>(); fun get(key: String)=rows[key];fun put(row: Meta){rows[row.key]=row.value} }
object Db { val dao=MetaDao();fun get(context: Context)=this;fun meta()=dao }
class MainActivity
class Intent(context: Context, target: Class<*>) {
 fun addFlags(flags: Int)=this
 companion object { const val FLAG_ACTIVITY_NEW_TASK=1;const val FLAG_ACTIVITY_CLEAR_TOP=2 }
}
object PendingIntent {
 const val FLAG_IMMUTABLE=1; const val FLAG_UPDATE_CURRENT=2
 fun getActivity(context: Context,id: Int,intent: Intent,flags: Int)=Any()
}
class RemoteViews(packageName: String, layout: Int) {
 val values=mutableMapOf<Int,String>()
 fun setOnClickPendingIntent(id: Int,pending: Any) {}
 fun setTextViewText(id: Int,text: String) { values[id]=text }
}
