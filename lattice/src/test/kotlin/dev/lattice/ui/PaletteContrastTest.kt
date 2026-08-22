package dev.lattice.ui

import dev.lattice.ui.token.LatPalettes
import dev.lattice.ui.token.MIN_READABLE_CONTRAST
import dev.lattice.ui.token.clashesWithDanger
import dev.lattice.ui.token.contrastRatio
import dev.lattice.ui.token.dangerFor
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
        for (spec in LatPalettes) {
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
        for (spec in LatPalettes) {
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
        for (spec in LatPalettes) {
            for (light in listOf(false, true)) {
                val palette = spec.palette(light)
                val danger = dangerFor(palette.background)
                assertTrue(
                    "danger on ${spec.id} (light=$light) is ${contrastRatio(danger, palette.background)}",
                    contrastRatio(danger, palette.background) >= 3.0,
                )
            }
        }
    }

    @Test
    fun `a red accent is reported as clashing with danger`() {
        // The reason the check exists: on a red palette, a red destructive action
        // is indistinguishable from an ordinary one.
        assertTrue(clashesWithDanger(0xFFE04A2FL.toInt()))
        assertFalse(clashesWithDanger(0xFF4A7DE0L.toInt()))
    }
}
