package dev.vespian.work

import kotlinx.coroutines.*

object R {
    object layout { const val widget_forecast = 1 }
    object id { const val widget_root=2; const val widget_value=3; const val widget_range=4; const val widget_gate=5 }
    object string { const val widget_none=6; const val widget_range=7; const val widget_gate=8 }
}
class MainActivity
class Context {
    val packageName = "fake.widget"
    var failText = false
    fun getString(id: Int, vararg args: Any): String {
        if (failText) throw IllegalStateException("private-health-payload")
        return when(id) { R.string.widget_none -> "Unavailable"; else -> args.joinToString("–") }
    }
}
class Intent(context: Context, type: Class<*>) {
    fun addFlags(flags: Int) = this
    companion object { const val FLAG_ACTIVITY_NEW_TASK=1; const val FLAG_ACTIVITY_CLEAR_TOP=2 }
}
object PendingIntent {
    const val FLAG_IMMUTABLE=1; const val FLAG_UPDATE_CURRENT=2
    fun getActivity(context: Context, code: Int, intent: Intent, flags: Int) = this
}
class RemoteViews(packageName: String, layout: Int) {
    val text = mutableMapOf<Int,String>()
    var clickable=false
    fun setOnClickPendingIntent(id: Int, intent: PendingIntent) { clickable=true }
    fun setTextViewText(id: Int, value: String) { text[id]=value }
}
class AppWidgetManager {
    val updates = mutableListOf<Pair<Int,RemoteViews>>()
    var fail = false
    fun updateAppWidget(id: Int, views: RemoteViews) {
        if (fail) throw IllegalStateException("secret-token")
        updates += id to views
    }
}
class Pending { var finishes=0; fun finish() { finishes++ } }
object Log {
    val entries = mutableListOf<String>()
    fun w(tag: String, message: String) { entries += "$tag: $message" }
}
data class Band(val median: Double=22.0, val low: Double=21.0, val high: Double=23.0, val confidence: Double=0.9)
class Calibration { fun confidence(onset: Band) = onset.confidence }
data class Forecast(val nights: Int=1, val onset: Band=Band(), val gate: Band=Band(), val calib: Calibration?=null)
object Engine {
    var action: suspend () -> Forecast = { Forecast() }
    var failFormatting=false
    suspend fun forecast(context: Context) = action()
    fun millisOf(hour: Double): Long {
        if (failFormatting) throw IllegalArgumentException("private-health-payload")
        return (hour * 3600000).toLong()
    }
}
