package dev.trial3lib.ui

import dev.trial3lib.ui.token.DEFAULT_TRIAL3_PALETTE_ID
import dev.trial3lib.ui.token.Trial3Palettes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Palettes are chosen from a list a person reads, so the list has to be legible.
 *
 * These names used to be keys like "set.118" that meant nothing without the
 * consuming app's string catalogue: a picker built against the library alone
 * showed a column of numbers. Names now live here in English and a translated
 * app overrides them, so the failure mode to guard against is a key sneaking
 * back in, or two palettes claiming the same identity.
 */
class PaletteNamingTest {

    @Test
    fun `palette ids are unique`() {
        val ids = Trial3Palettes.map { it.id }
        assertEquals("two palettes share an id", ids.size, ids.toSet().size)
    }

    @Test
    fun `palette names are unique`() {
        val names = Trial3Palettes.map { it.defaultName }
        assertEquals("two palettes share a name", names.size, names.toSet().size)
    }

    @Test
    fun `every palette has a readable name and not a lookup key`() {
        val readable = Regex("[A-Z][A-Za-z]+")
        for (spec in Trial3Palettes) {
            val name = spec.defaultName
            assertTrue("palette ${spec.id} has no name", name.isNotBlank())
            assertTrue(
                "palette ${spec.id} is named \"$name\", which looks like a catalogue key",
                readable.matches(name),
            )
        }
    }

    @Test
    fun `the default palette is one of the palettes`() {
        assertTrue(
            "default id $DEFAULT_TRIAL3_PALETTE_ID is not in the list",
            Trial3Palettes.any { it.id == DEFAULT_TRIAL3_PALETTE_ID },
        )
    }

    @Test
    fun `ids are lower case so they are stable in stored settings`() {
        for (spec in Trial3Palettes) {
            assertEquals("id ${spec.id} is not lower case", spec.id.lowercase(), spec.id)
        }
    }
}
