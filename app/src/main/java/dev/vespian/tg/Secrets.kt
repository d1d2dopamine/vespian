package dev.vespian.tg

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Encrypted-only bot credentials. Existing legacy plaintext is never read, copied or deleted. */
object Secrets {
    private const val FILE = "vespian_secrets"
    private const val K_TOKEN = "tg_token"
    private const val K_CHAT = "tg_chat"
    private val lock = Any()
    private var cached: SharedPreferences? = null
    @Volatile var storageUnavailable: Boolean = false
        private set

    private fun failed() {
        cached = null
        storageUnavailable = true
    }

    // Caller holds lock. A failure remains closed until an explicit read-only retry
    // or a fresh process start; background reads cannot silently resume access.
    private fun prefs(context: Context): SharedPreferences? {
        if (storageUnavailable) return null
        cached?.let { return it }
        return try {
            val app = context.applicationContext
            val key = MasterKey.Builder(app).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(app, FILE, key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM).also { cached = it }
        } catch (_: Exception) {
            failed()
            null
        }
    }

    private fun pair(context: Context): Pair<String, String>? = synchronized(lock) {
        val p = prefs(context) ?: return@synchronized null
        try {
            p.getString(K_TOKEN, "").orEmpty().trim() to p.getString(K_CHAT, "").orEmpty().trim()
        } catch (_: Exception) {
            failed()
            null
        }
    }

    fun token(context: Context): String = pair(context)?.first.orEmpty()
    fun chatId(context: Context): String = pair(context)?.second.orEmpty()
    fun configured(context: Context): Boolean = pair(context)?.let { it.first.isNotEmpty() && it.second.isNotEmpty() } ?: false

    /** Explicit user retry: access existing encrypted data without deleting or overwriting it. */
    fun retry(context: Context): Boolean = synchronized(lock) {
        cached = null
        storageUnavailable = false
        pair(context) != null
    }

    /** Call off the main thread. True only after encrypted storage confirms the write. */
    fun save(context: Context, token: String, chatId: String): Boolean = synchronized(lock) {
        // A failed read may have left blank UI fields. Do not use Save as an
        // implicit recovery/overwrite path: require a successful read-only retry.
        if (storageUnavailable) return@synchronized false
        val p = prefs(context) ?: return@synchronized false
        if (pair(context) == null) return@synchronized false
        try {
            val saved = p.edit().putString(K_TOKEN, token.trim()).putString(K_CHAT, chatId.trim()).commit()
            if (!saved) failed()
            saved
        } catch (_: Exception) {
            failed()
            false
        }
    }

    /** Explicit encrypted-store removal only; never touches any legacy plaintext file. */
    fun clear(context: Context): Boolean = synchronized(lock) {
        val p = prefs(context) ?: return@synchronized false
        try {
            val cleared = p.edit().remove(K_TOKEN).remove(K_CHAT).commit()
            if (!cleared) failed()
            cleared
        } catch (_: Exception) {
            failed()
            false
        }
    }

    fun masked(context: Context): String {
        val t = token(context)
        if (t.isEmpty()) return ""
        val head = t.substringBefore(':', t.take(6))
        return head + ":" + "*".repeat(8) + t.takeLast(3)
    }
}
