package dev.vespian.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.lattice.ui.LatticeTheme
import dev.lattice.ui.token.LatPalette

// Palette. Deliberately no violet anywhere.
// Night sky blues for surfaces, teal for the primary signal,
// amber for the warm accent that marks "act now" moments.
//
// These four names are still read directly by the charts (ui/Plots.kt) and the
// ring (ui/Ring.kt), which draw with Canvas and pick their own colours, so they
// stay exactly as they were.

val Teal = Color(0xFF4FD1C5)
val TealDim = Color(0xFF2B8A80)
val Amber = Color(0xFFFFB74D)
val AmberDim = Color(0xFF8A6224)
val Coral = Color(0xFFFF7B72)
val Ink = Color(0xFF0A1018)
val Slate = Color(0xFF141E2C)
val SlateHi = Color(0xFF1D2A3C)
val Mist = Color(0xFFE4EAF2)
val MistDim = Color(0xFF97A6BA)

// Four colours instead of thirty. Material's scheme asked for primaryContainer,
// onSecondaryContainer, surfaceVariant and two dozen more, and most of them were
// filled in by hand with values nothing on screen ever read. Lattice takes the
// background, the text, the quiet text and the accent, and derives the panel
// tone and the hairline from them -- so a panel is always exactly one step away
// from the page it sits on, in both lightings, and cannot drift.
//
// Slate and SlateHi are therefore no longer wired into the theme: the panel tone
// is background mixed 7% toward the ink, which on Ink lands within a shade of
// the old Slate.
private val NightPalette = LatPalette(
    background = Ink,
    ink = Mist,
    muted = MistDim,
    accent = Teal,
)

// Day is the same brand in the other lighting: the accent darkens so it still
// clears 4.5:1 against paper, which the bright teal does not.
private val DayPalette = LatPalette(
    background = Color(0xFFF6F8FC),
    ink = Color(0xFF10161F),
    muted = Color(0xFF4A5769),
    accent = Color(0xFF00695C),
)

/**
 * Wrap the app in this, once per Activity.
 *
 * The signature has not changed, so every call site is untouched. What changed
 * is underneath: this is Lattice -- Ikna's design system, extracted into its own
 * module -- instead of Material 3. No dynamic colour, for the reason that was
 * always written here: Material You derives every role from one wallpaper hue,
 * primary, secondary and tertiary collapse into neighbouring tones, and the day
 * ring separates three arcs by colour alone.
 *
 * Type is Lattice's scale, and it is applied to all fifteen slots at once, so a
 * user font cannot land on the body text and miss the titles.
 */
@Composable
fun VespianTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    LatticeTheme(
        palette = if (dark) NightPalette else DayPalette,
        content = content,
    )
}
