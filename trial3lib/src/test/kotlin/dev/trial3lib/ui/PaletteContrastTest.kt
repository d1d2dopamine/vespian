package dev.trial3lib.ui

import androidx.compose.ui.graphics.Color
import dev.trial3lib.ui.token.Trial3Palettes
import dev.trial3lib.ui.token.MIN_READABLE_CONTRAST
import dev.trial3lib.ui.token.clashesWithDanger
import dev.trial3lib.ui.token.contrastRatio
import dev.trial3lib.ui.token.dangerFor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every palette that ships must be readable in both lightings.
 *
 * This is the test that makes the palette list safe to extend: a new set can be
 * added by anyone, and if its muted tone is a pretty grey that cannot be read on
 * its own background, the build says so instead of a user finding out.
 */
class PaletteContrastTest {

    @Test
    fun `body text is readable on its background in every palette`() {
        for (spec in Trial3Palettes) {
            for (light in listOf(false, true)) {
                val palette = spec.palette(light)
                val ink = contrastRatio(palette.ink, palette.background)
                assertTrue(
                    "ink on background in ${spec.id} (light=$light) is $ink",
                    ink >= MIN_READABLE_CONTRAST,
                )
            }
        }
    }

    @Test
    fun `secondary text stays above the readable floor`() {
        // 3.0 is the large-text floor: the muted tone is only ever used for
        // labels and captions, never for a paragraph.
        for (spec in Trial3Palettes) {
            for (light in listOf(false, true)) {
                val palette = spec.palette(light)
                val muted = contrastRatio(palette.muted, palette.background)
                assertTrue(
                    "muted on background in ${spec.id} (light=$light) is $muted",
                    muted >= 3.0,
                )
            }
        }
    }

    @Test
    fun `the danger colour is never invisible on its own palette`() {
        // dangerFor takes the whole palette, not just its background: when the
        // accent is itself red it returns the ink colour instead, so a red
        // destructive action never sits on a red palette.
        for (spec in Trial3Palettes) {
            for (light in listOf(false, true)) {
                val palette = spec.palette(light)
                val ratio = contrastRatio(dangerFor(palette), palette.background)
                assertTrue(
                    "danger on ${spec.id} (light=$light) is $ratio",
                    ratio >= 3.0,
                )
            }
        }
    }

    @Test
    fun `a red accent is reported as clashing with danger`() {
        // The reason the check exists: on a red palette, a red destructive action
        // is indistinguishable from an ordinary one.
        assertTrue(clashesWithDanger(Color(0xFFE04A2F)))
        assertFalse(clashesWithDanger(Color(0xFF4A7DE0)))
    }

    @Test
    fun `a desaturated warm grey is not treated as red`() {
        // The saturation floor matters as much as the hue window: a warm grey
        // accent sits near red on the wheel but reads as neutral.
        assertFalse(clashesWithDanger(Color(0xFF8A7F7A)))
    }
}
