package dev.vespian.ui

class Particle(val latency: Double = 1.0)
class Filter(val particles: List<Particle> = listOf(Particle())) {
    fun debtBandMinutes() = Band()
}
class Band(val median: Double = 1.0)
class Night(val sleepEnd: Long = 1L)
class Nap(val minutes: Int = 1)
object Engine {
    var forecastCall: suspend () -> String = { "fresh" }
    var loadCall: suspend () -> Filter = { Filter() }
    var obsCall: suspend () -> IntArray = { intArrayOf(1) }
    suspend fun forecast(context: Unit) = forecastCall()
    suspend fun load(context: Unit) = loadCall()
    suspend fun obsStats(context: Unit) = obsCall()
}
object PredLog {
    var call: suspend () -> List<String> = { listOf("fresh") }
    suspend fun history(context: Unit, count: Int) = call()
}
object Delay { suspend fun estimate(context: Unit): Int = 1 }
object Drinks {
    fun dayStart() = 0L
    suspend fun anyToday(context: Unit) = true
    suspend fun settled(context: Unit) = true
}
object Db {
    var countCall: suspend () -> Int = { 2 }
    fun get(context: Unit) = this
    fun nights() = this
    fun naps() = this
    suspend fun lastEnded(time: Long): Night? = null
    suspend fun between(from: Long, to: Long): List<Nap> = emptyList()
    suspend fun count() = countCall()
}
object UiCache {
    var forecast: String? = null
    var latency: Double? = null
    var obs: IntArray? = null
    var delay: Int? = null
    var debt: Int? = null
    var history: List<String>? = null
    var filter: Filter? = null
    var nights = 0
    fun reset() {
        forecast=null; latency=null; obs=null; delay=null; debt=null; history=null; filter=null; nights=0
        Engine.forecastCall={ "fresh" }; Engine.loadCall={ Filter() }; Engine.obsCall={ intArrayOf(1) }
        PredLog.call={ listOf("fresh") }; Db.countCall={ 2 }
    }
}
open class Fields {
    val context = Unit
    val HISTORY_NIGHTS = 14
    var forecast: String? = null
    var failed = false
    var latency: Double? = null
    var obs: IntArray? = null
    var delay: Int? = null
    var debt: Int? = null
    var staleDays = 0
    var napMinutes = 0
    var unanswered = false
    var history: List<String>? = null
    var filter: Filter? = null
    var nights = 0
    internal var forecastLoad = LoadState<String>(null)
    internal var historyLoad = LoadState<List<String>>(null)
    internal var modelLoad = LoadState<Pair<Filter, Int>>(null)
}
