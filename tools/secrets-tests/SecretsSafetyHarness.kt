package dev.vespian.tg
import android.content.Context
import androidx.security.crypto.CryptoBoundary
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SecretsSafetyHarness {
    private lateinit var context: Context
    @Before fun reset() {
        context = Context()
        CryptoBoundary.reset()
        for ((name, value) in listOf("cached" to null, "storageUnavailable" to false)) {
            Secrets::class.java.getDeclaredField(name).apply { isAccessible = true }.set(null, value)
        }
        context.plain.values["tg_token"] = "legacy-sentinel"
        context.plain.values["tg_chat"] = "legacy-chat-sentinel"
    }
    @Test fun `encrypted read works without opening plaintext`() {
        CryptoBoundary.encrypted.values["tg_token"] = "encrypted-sentinel"
        CryptoBoundary.encrypted.values["tg_chat"] = "chat-sentinel"
        assertTrue(Secrets.configured(context))
        assertEquals("encrypted-sentinel", Secrets.token(context))
        assertEquals(0, context.plaintextOpens)
    }
    @Test fun `keystore failure fails closed and preserves legacy file`() {
        val before = context.plain.values.toMap()
        CryptoBoundary.failMasterKey = true
        assertEquals("", Secrets.token(context)); assertFalse(Secrets.configured(context))
        assertTrue(Secrets.storageUnavailable); assertEquals(0, context.plaintextOpens)
        assertEquals(before, context.plain.values)
    }
    @Test fun `encrypted open failure cannot fall back`() {
        CryptoBoundary.failOpen = true
        assertEquals("", Secrets.chatId(context)); assertTrue(Secrets.storageUnavailable)
        assertEquals(0, context.plaintextOpens)
    }
    @Test fun `decryption read failure exposes neither member of pair`() {
        CryptoBoundary.encrypted.values["tg_token"] = "encrypted-sentinel"
        CryptoBoundary.encrypted.failRead = true
        assertEquals("", Secrets.token(context)); assertEquals("", Secrets.chatId(context))
        assertFalse(Secrets.configured(context)); assertTrue(Secrets.storageUnavailable)
    }
    @Test fun `background reads cannot auto recover a failed store`() {
        CryptoBoundary.failOpen = true; Secrets.token(context)
        val calls = CryptoBoundary.opens
        CryptoBoundary.failOpen = false
        repeat(5) { assertFalse(Secrets.configured(context)) }
        assertEquals(calls, CryptoBoundary.opens)
        assertTrue(Secrets.storageUnavailable)
    }
    @Test fun `explicit retry recovers existing encrypted values without writing`() {
        CryptoBoundary.encrypted.values["tg_token"] = "encrypted-sentinel"
        CryptoBoundary.encrypted.values["tg_chat"] = "chat-sentinel"
        val before = CryptoBoundary.encrypted.values.toMap()
        CryptoBoundary.failOpen = true; Secrets.token(context)
        CryptoBoundary.failOpen = false
        assertTrue(Secrets.retry(context)); assertTrue(Secrets.configured(context))
        assertEquals(before, CryptoBoundary.encrypted.values)
    }
    @Test fun `persistent failure remains recoverable error after retry`() {
        CryptoBoundary.failOpen = true
        assertFalse(Secrets.retry(context)); assertTrue(Secrets.storageUnavailable)
        assertEquals(0, context.plaintextOpens)
    }
    @Test fun `successful save commits encrypted values only`() {
        assertTrue(Secrets.save(context, " sentinel ", " chat "))
        assertEquals("sentinel", CryptoBoundary.encrypted.values["tg_token"])
        assertEquals("chat", CryptoBoundary.encrypted.values["tg_chat"])
        assertEquals("legacy-sentinel", context.plain.values["tg_token"])
        assertEquals(0, context.plaintextOpens)
    }
    @Test fun `failed save never reports ready or uses plaintext`() {
        CryptoBoundary.failOpen = true
        assertFalse(Secrets.save(context, "sentinel", "chat"))
        assertTrue(Secrets.storageUnavailable); assertFalse(Secrets.configured(context))
        assertEquals(0, context.plaintextOpens)
    }
    @Test fun `failed durable commit stays closed despite in memory values`() {
        CryptoBoundary.encrypted.commitResult = false
        assertFalse(Secrets.save(context, "sentinel", "chat"))
        assertEquals("sentinel", CryptoBoundary.encrypted.values["tg_token"])
        assertEquals("", Secrets.token(context)); assertFalse(Secrets.configured(context))
    }
    @Test fun `editor and commit exceptions are recoverable errors`() {
        CryptoBoundary.encrypted.failEdit = true
        assertFalse(Secrets.save(context, "sentinel", "chat"))
        CryptoBoundary.encrypted.failEdit = false
        assertTrue(Secrets.retry(context))
        CryptoBoundary.encrypted.failCommit = true
        assertFalse(Secrets.save(context, "sentinel", "chat"))
        assertTrue(Secrets.storageUnavailable)
    }
    @Test fun `blank fields after failed read cannot overwrite recovered credentials`() {
        CryptoBoundary.encrypted.values["tg_token"] = "existing-sentinel"
        CryptoBoundary.encrypted.values["tg_chat"] = "existing-chat"
        val before = CryptoBoundary.encrypted.values.toMap()
        CryptoBoundary.encrypted.failRead = true
        assertEquals("", Secrets.token(context))
        CryptoBoundary.encrypted.failRead = false
        assertFalse(Secrets.save(context, "", ""))
        assertEquals(before, CryptoBoundary.encrypted.values)
        assertTrue(Secrets.retry(context))
        assertEquals("existing-sentinel", Secrets.token(context))
    }
    @Test fun `empty encrypted store never imports existing legacy credentials`() {
        assertFalse(Secrets.configured(context)); assertFalse(Secrets.storageUnavailable)
        assertEquals(0, context.plaintextOpens)
        assertEquals("legacy-sentinel", context.plain.values["tg_token"])
    }
    @Test fun `explicit clear affects encrypted values only`() {
        assertTrue(Secrets.save(context, "sentinel", "chat"))
        assertTrue(Secrets.clear(context)); assertFalse(Secrets.configured(context))
        assertEquals("legacy-sentinel", context.plain.values["tg_token"])
        assertEquals(0, context.plaintextOpens)
    }
}
