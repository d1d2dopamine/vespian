package android.content

interface SharedPreferences {
    fun getString(key: String, default: String?): String?
    fun edit(): Editor
    interface Editor {
        fun putString(key: String, value: String?): Editor
        fun remove(key: String): Editor
        fun commit(): Boolean
        fun apply()
    }
}
class FakePreferences : SharedPreferences {
    val values = mutableMapOf<String, String?>()
    var failRead = false
    var failEdit = false
    var failCommit = false
    var commitResult = true
    override fun getString(key: String, default: String?): String? {
        if (failRead) error("synthetic read failure")
        return if (values.containsKey(key)) values[key] else default
    }
    override fun edit(): SharedPreferences.Editor {
        if (failEdit) error("synthetic editor failure")
        return object : SharedPreferences.Editor {
            val put = mutableMapOf<String, String?>()
            val remove = mutableSetOf<String>()
            override fun putString(key: String, value: String?) = apply { put[key] = value }
            override fun remove(key: String) = apply { remove.add(key) }
            override fun commit(): Boolean {
                if (failCommit) error("synthetic commit failure")
                // Failed durable writes can still update in-process preferences.
                remove.forEach { values.remove(it) }; values.putAll(put)
                return commitResult
            }
            override fun apply() { commit() }
        }
    }
}
open class Context {
    val applicationContext: Context get() = this
    val plain = FakePreferences()
    var plaintextOpens = 0
    fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        require(name == "vespian_secrets_plain")
        plaintextOpens++
        return plain
    }
    companion object { const val MODE_PRIVATE = 0 }
}
