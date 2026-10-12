package dev.vespian.tg
import android.content.Context
import androidx.security.crypto.CryptoBoundary
import org.junit.Assert.*
import org.junit.Test
/** Expected red against unmodified stage3 Secrets.kt, with synthetic sentinels only. */
class LegacySecretsProbe {
    @Test fun `encryption failure must not open plaintext fallback`() {
        Secrets::class.java.getDeclaredField("cached").apply { isAccessible = true }.set(null, null)
        val context = Context()
        context.plain.values["tg_token"] = "legacy-sentinel"
        context.plain.values["tg_chat"] = "chat-sentinel"
        CryptoBoundary.reset(); CryptoBoundary.failOpen = true
        Secrets.token(context)
        assertEquals(0, context.plaintextOpens)
    }
}
