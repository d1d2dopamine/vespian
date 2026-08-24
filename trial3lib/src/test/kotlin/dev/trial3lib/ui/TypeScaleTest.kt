package dev.trial3lib.ui

import androidx.compose.ui.text.font.FontFamily
import dev.trial3lib.ui.token.Trial3Typography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The font a user picks must reach every line of text.
 *
 * This is the test for the bug that motivated the file: a type scale that is
 * assembled slot by slot will eventually gain a slot that nobody remembers to
 * apply the font to, and that slot silently falls back to the platform default.
 * Here the scale is enumerated, so a forgotten slot is a failing test.
 *
 * all() returns each style paired with its name, so a failure names the slot
 * that broke instead of only counting how many did.
 */
class TypeScaleTest {

    @Test
    fun `withFont reaches every style in the scale`() {
        val family = FontFamily.Monospace
        val typography = Trial3Typography().withFont(family)
        val missed = typography.all()
            .filter { (_, style) -> style.fontFamily != family }
            .map { (name, _) -> name }
        assertTrue("styles that kept the default font: $missed", missed.isEmpty())
    }

    @Test
    fun `withFont returns the scale unchanged when no font is chosen`() {
        val plain = Trial3Typography()
        assertEquals(plain, plain.withFont(null))
    }

    @Test
    fun `the scale exposes every slot it defines`() {
        // all() is what the test above relies on, so it must not silently omit a
        // style that the data class declares.
        assertEquals(15, Trial3Typography().all().size)
    }

    @Test
    fun `every slot is listed under its own name`() {
        val names = Trial3Typography().all().map { (name, _) -> name }
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    fun `line height is never smaller than the glyph size`() {
        for ((name, style) in Trial3Typography().all()) {
            assertTrue(
                "$name: line height ${style.lineHeight} under font size ${style.fontSize}",
                style.lineHeight.value >= style.fontSize.value,
            )
        }
    }
}
