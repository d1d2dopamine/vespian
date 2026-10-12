package androidx.security.crypto
import android.content.Context
import android.content.FakePreferences
import android.content.SharedPreferences
object CryptoBoundary {
    var encrypted = FakePreferences()
    var failMasterKey = false
    var failOpen = false
    var opens = 0
    fun reset() { encrypted = FakePreferences(); failMasterKey = false; failOpen = false; opens = 0 }
}
class MasterKey {
    enum class KeyScheme { AES256_GCM }
    class Builder(context: Context) {
        fun setKeyScheme(scheme: KeyScheme) = this
        fun build(): MasterKey {
            if (CryptoBoundary.failMasterKey) error("synthetic keystore failure")
            return MasterKey()
        }
    }
}
object EncryptedSharedPreferences {
    enum class PrefKeyEncryptionScheme { AES256_SIV }
    enum class PrefValueEncryptionScheme { AES256_GCM }
    fun create(context: Context, file: String, key: MasterKey, keyScheme: PrefKeyEncryptionScheme,
               valueScheme: PrefValueEncryptionScheme): SharedPreferences {
        CryptoBoundary.opens++
        if (CryptoBoundary.failOpen) error("synthetic encrypted-store failure")
        return CryptoBoundary.encrypted
    }
}
