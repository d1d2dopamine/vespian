package dev.lattice.ui

import androidx.compose.ui.text.font.FontFamily
import dev.lattice.ui.token.LatTypography
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
 */
class TypeScaleTest {

    @Test
    fun `withFont reaches every style in the scale`() {
        val family = FontFamily.Monospace
        val typography = LatTypography().withFont(family)
        val missed = typography.all().filter { it.fontFamily != family }
        assertTrue("styles that kept the default font: ${missed.size}", missed.isEmpty())
    }

    @Test
    fun `the scale exposes every slot it defines`() {
        // all() is what the test above relies on, so it must not silently omit a
        // style that the data class declares.
        assertEquals(15, LatTypography().all().size)
    }

    @Test
    fun `line height is never smaller than the glyph size`() {
        for (style in LatTypography().all()) {
            assertTrue(
                "line height ${style.lineHeight} under font size ${style.fontSize}",
                style.lineHeight.value >= style.fontSize.value,
            )
        }
    }
}
