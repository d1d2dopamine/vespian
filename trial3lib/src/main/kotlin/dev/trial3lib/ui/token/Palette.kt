package dev.trial3lib.ui.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.min

/*
 * Paper and ink, one accent, right angles.
 *
 * What this deliberately is not: a Material 3 default. No dynamic colour from
 * the wallpaper, no rounded corners, no elevation tints, no green/red pair on
 * answer buttons. Punishment colours are a bad idea in general and a worse one
 * in an app somebody uses every day.
 *
 * A theme is exactly four colours -- background, ink, muted, accent -- and
 * everything else is derived from them. That is not minimalism for its own sake:
 * it is what makes a user-defined theme possible at all. Panel and line are
 * mixed from the first two rather than picked, so a hand-made palette cannot end
 * up with a panel that is invisible against its own background.
 *
 * A palette is not a theme. The same palette exists in both lightings and keeps
 * its hue in both, so the light version is tinted paper rather than white with
 * the colour drained out. Each light accent is a darker, deeper version of the
 * same hue, never the dark value reused: an accent that glows on a near-black
 * field measures about 2:1 on paper.
 */

// ---- danger ----------------------------------------------------------------
//
// Reserved for destructive controls only: wiping data, starting over.
//
// Deliberately not part of a palette -- "this one is dangerous" has to survive
// any colour scheme, including one the user invented. But it cannot be a single
// constant either. A red that measures 3.5:1 on a dark field is below the line
// for text on the one control where a misread is unrecoverable, so there are
// two, one per lighting. And a red warning stops being a warning next to a warm
// accent: when the accent is that close in hue, danger is carried by the word
// and the frame instead and the colour steps back to ink. Colour is never
// allowed to be the only carrier of meaning anyway.
private val DangerOnDark = Color(0xFFFF7A66)
private val DangerOnLight = Color(0xFF962A17)

private const val DANGER_HUE = 8f
private const val DANGER_HUE_GUARD = 26f
private const val DANGER_MIN_SATURATION = 0.35f

/** True when an accent is close enough to the danger red to be mistaken for it. */
public fun clashesWithDanger(accent: Color): Boolean {
    val (hue, saturation) = hueAndSaturation(accent)
    if (saturation < DANGER_MIN_SATURATION) return false
    val distance = abs(hue - DANGER_HUE)
    return min(distance, 360f - distance) < DANGER_HUE_GUARD
}

/** The colour of a destructive control under this palette. */
public fun dangerFor(palette: Trial3Palette): Color {
    if (clashesWithDanger(palette.accent)) return palette.ink
    return if (palette.light) DangerOnLight else DangerOnDark
}

/**
 * A whole theme: four colours in, everything else out.
 */
@Immutable
public data class Trial3Palette(
    val background: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
) {
    /** Surfaces that need to be a step away from the background. */
    public val panel: Color get() = mixColors(background, ink, 0.07f)

    /** Borders and rules. */
    public val line: Color get() = mixColors(background, ink, 0.28f)

    /** Drives the lighting-dependent choices and the system bar icons. */
    public val light: Boolean get() = isLightColor(background)

    /** The colour a destructive control takes under this palette. */
    public val danger: Color get() = dangerFor(this)
}

/**
 * A palette and both of its lightings, authored by hand rather than derived.
 *
 * A light theme computed by inverting a dark one comes out brown: a warm muted
 * grey that works on near-black is a smudge on paper. [defaultName] is the plain
 * English name. An app that is translated installs a [Trial3PaletteNamer] and
 * gets its own wording, so this list never has to know any language.
 */
@Immutable
public data class Trial3PaletteSpec(
    val id: String,
    val defaultName: String,
    val dark: Trial3Palette,
    val light: Trial3Palette,
) {
    public fun palette(light: Boolean): Trial3Palette = if (light) this.light else this.dark
}

/**
 * Every palette shipped, in the order they are offered.
 *
 * Each one is a hue in the background with paper of the same hue as its light
 * version: one brand in two lightings. "zero" is the exception on purpose --
 * pure black and pure white, for sunlight and for OLED, where the recognisable
 * part is the absence of colour.
 *
 * Every entry here is asserted readable in both lightings by PaletteContrastTest.
 */
public val Trial3Palettes: List<Trial3PaletteSpec> = listOf(
    // ink: the default. Ink-blue paper with a warm accent on it.
    Trial3PaletteSpec(
        id = "ink",
        defaultName = "Ink",
        dark = Trial3Palette(
            background = Color(0xFF0B1120),
            ink = Color(0xFFE5EAF4),
            muted = Color(0xFF78859C),
            accent = Color(0xFFFF7A5C),
        ),
        light = Trial3Palette(
            background = Color(0xFFEDF1F8),
            ink = Color(0xFF0E1526),
            muted = Color(0xFF58637A),
            // Two steps deeper than the coral above, not one: the obvious
            // #C9452B looks right next to it and measures 4.2:1 on this paper,
            // which is under the line for the small mono labels.
            accent = Color(0xFFB83A21),
        ),
    ),
    // library: bottle green and brass. A reading room, nothing hurrying.
    Trial3PaletteSpec(
        id = "library",
        defaultName = "Library",
        dark = Trial3Palette(
            background = Color(0xFF0F1712),
            ink = Color(0xFFE8EEE4),
            muted = Color(0xFF7D9083),
            accent = Color(0xFFE3B45E),
        ),
        light = Trial3Palette(
            background = Color(0xFFEFF2E8),
            ink = Color(0xFF14201A),
            muted = Color(0xFF5C6D61),
            accent = Color(0xFF7E6413),
        ),
    ),
    // ember: burnt earth and an ember.
    Trial3PaletteSpec(
        id = "ember",
        defaultName = "Ember",
        dark = Trial3Palette(
            background = Color(0xFF17100C),
            ink = Color(0xFFF2E6D9),
            muted = Color(0xFF9C8574),
            accent = Color(0xFFF2683C),
        ),
        light = Trial3Palette(
            background = Color(0xFFF7EADC),
            ink = Color(0xFF241610),
            muted = Color(0xFF7A5B49),
            accent = Color(0xFFB8431F),
        ),
    ),
    // plum: the loudest one. Aubergine and mint.
    Trial3PaletteSpec(
        id = "plum",
        defaultName = "Plum",
        dark = Trial3Palette(
            background = Color(0xFF150F1C),
            ink = Color(0xFFEDE6F1),
            muted = Color(0xFF897D94),
            accent = Color(0xFF45D6A6),
        ),
        light = Trial3Palette(
            background = Color(0xFFF4EFF7),
            ink = Color(0xFF1D1324),
            muted = Color(0xFF695B74),
            accent = Color(0xFF0C7A59),
        ),
    ),
    // rose: pink without the sugar. A wine-dark field and a rose that reads as a
    // highlighter. The accent sits at hue 333 deliberately: twenty-five degrees
    // further round the wheel is the danger guard, and an accent that trips it
    // hands the warning colour back to the ink -- a palette with one colour less.
    Trial3PaletteSpec(
        id = "rose",
        defaultName = "Rose",
        dark = Trial3Palette(
            background = Color(0xFF1A0E15),
            ink = Color(0xFFF6E7EE),
            muted = Color(0xFFA2808F),
            accent = Color(0xFFFF7FB8),
        ),
        light = Trial3Palette(
            background = Color(0xFFFBEEF3),
            ink = Color(0xFF26121C),
            muted = Color(0xFF7C5566),
            accent = Color(0xFFA81E5E),
        ),
    ),
    // frost: the one palette with nothing warm in it anywhere.
    Trial3PaletteSpec(
        id = "frost",
        defaultName = "Frost",
        dark = Trial3Palette(
            background = Color(0xFF08131A),
            ink = Color(0xFFE2EEF3),
            muted = Color(0xFF7C929C),
            accent = Color(0xFF5FD2E8),
        ),
        light = Trial3Palette(
            background = Color(0xFFECF3F6),
            ink = Color(0xFF0C1B22),
            muted = Color(0xFF516771),
            accent = Color(0xFF0C6072),
        ),
    ),
    // phosphor: the ink itself is the colour, so the whole surface is one hue and
    // the accent is merely a brighter pass of it.
    Trial3PaletteSpec(
        id = "phosphor",
        defaultName = "Phosphor",
        dark = Trial3Palette(
            background = Color(0xFF040A06),
            ink = Color(0xFFB8F5CB),
            muted = Color(0xFF6DA981),
            accent = Color(0xFF4AF08C),
        ),
        light = Trial3Palette(
            background = Color(0xFFEEF6EF),
            ink = Color(0xFF0A1E11),
            muted = Color(0xFF4E6B58),
            accent = Color(0xFF116B36),
        ),
    ),
    // zero: no hue at all, in either direction.
    Trial3PaletteSpec(
        id = "zero",
        defaultName = "Zero",
        dark = Trial3Palette(
            background = Color(0xFF000000),
            ink = Color(0xFFFFFFFF),
            muted = Color(0xFF8F8F8F),
            accent = Color(0xFFFFFFFF),
        ),
        light = Trial3Palette(
            background = Color(0xFFFFFFFF),
            ink = Color(0xFF000000),
            muted = Color(0xFF6E6E6E),
            accent = Color(0xFF000000),
        ),
    ),
    // neutral: grey that gets out of the way.
    Trial3PaletteSpec(
        id = "neutral",
        defaultName = "Neutral",
        dark = Trial3Palette(
            background = Color(0xFF121110),
            ink = Color(0xFFEDE9E1),
            muted = Color(0xFF8F887A),
            accent = Color(0xFF97A4D8),
        ),
        light = Trial3Palette(
            background = Color(0xFFFBFAF8),
            ink = Color(0xFF2C2A27),
            muted = Color(0xFF6B685F),
            accent = Color(0xFF33469E),
        ),
    ),
    // ultraviolet: purple is the accent as well as the atmosphere.
    Trial3PaletteSpec(
        id = "ultraviolet",
        defaultName = "Ultraviolet",
        dark = Trial3Palette(
            background = Color(0xFF110C24),
            ink = Color(0xFFF0E9FF),
            muted = Color(0xFF9787B5),
            accent = Color(0xFFC29BFF),
        ),
        light = Trial3Palette(
            background = Color(0xFFF3EEFC),
            ink = Color(0xFF1C1230),
            muted = Color(0xFF6C5D83),
            accent = Color(0xFF6D32C4),
        ),
    ),
    // lagoon: a green-blue field with no brass or coral in it.
    Trial3PaletteSpec(
        id = "lagoon",
        defaultName = "Lagoon",
        dark = Trial3Palette(
            background = Color(0xFF071918),
            ink = Color(0xFFE2F5F1),
            muted = Color(0xFF7F9F99),
            accent = Color(0xFF69E0C0),
        ),
        light = Trial3Palette(
            background = Color(0xFFEDF7F4),
            ink = Color(0xFF10231F),
            muted = Color(0xFF58746E),
            accent = Color(0xFF08705D),
        ),
    ),
    // cobalt: the ink background family with the temperature reversed.
    Trial3PaletteSpec(
        id = "cobalt",
        defaultName = "Cobalt",
        dark = Trial3Palette(
            background = Color(0xFF0A132B),
            ink = Color(0xFFEAF0FF),
            muted = Color(0xFF8491B5),
            accent = Color(0xFFFFD45A),
        ),
        light = Trial3Palette(
            background = Color(0xFFEEF2FF),
            ink = Color(0xFF121A34),
            muted = Color(0xFF5D6888),
            accent = Color(0xFF6E5700),
        ),
    ),
)

/** The id a clean install starts on. */
public const val DEFAULT_TRIAL3_PALETTE_ID: String = "ink"

/** The palette behind a stored id. Anything unknown resolves to the default. */
public fun trial3PaletteSpec(id: String): Trial3PaletteSpec =
    Trial3Palettes.firstOrNull { it.id == id }
        ?: Trial3Palettes.first { it.id == DEFAULT_TRIAL3_PALETTE_ID }

public val DefaultTrial3PaletteSpec: Trial3PaletteSpec get() = trial3PaletteSpec(DEFAULT_TRIAL3_PALETTE_ID)

public val Trial3DarkPalette: Trial3Palette get() = DefaultTrial3PaletteSpec.dark

public val Trial3LightPalette: Trial3Palette get() = DefaultTrial3PaletteSpec.light

/**
 * Eight colours a list item's square can be given, and nothing else.
 *
 * A free colour picker is the obvious alternative and the wrong one: the square
 * is read against twelve palettes in two lightings, and a hand-picked hex is one
 * slider away from an item that is invisible on the screen its owner actually
 * uses. These eight are all mid-tone -- none disappears into a near-black field
 * or into a paper-white one. The order is a rainbow rather than an internal list,
 * because this is picked by eye from a row of squares and nobody scans that row
 * by name. Store the index, never the colour: an index survives a palette being
 * retuned in a later version.
 */
public val Trial3Tints: List<Color> = listOf(
    Color(0xFFE5484D), // red
    Color(0xFFF2683C), // ember
    Color(0xFFF5A524), // amber
    Color(0xFF46A758), // green
    Color(0xFF12A594), // teal
    Color(0xFF3E9BFF), // blue
    Color(0xFF8E7BFF), // violet
    Color(0xFFE93D82), // pink
)

/** No tint chosen. */
public const val TRIAL3_NO_TINT: Int = -1

/**
 * The colour for a stored index, or [fallback] when none was chosen.
 *
 * Out-of-range indexes fall back rather than crashing: a settings file written by
 * a build with more colours than this one is a decoration that did not survive,
 * not a reason to refuse to draw the screen.
 */
public fun trial3TintColor(index: Int, fallback: Color): Color =
    if (index == TRIAL3_NO_TINT || index !in Trial3Tints.indices) fallback else Trial3Tints[index]

/**
 * Turns a palette id into a name in the language the reader is reading.
 *
 * A library cannot know what "Ultraviolet" is called in Turkish, and shipping a
 * translation table for twelve words would make every app carry twelve
 * languages it does not use. So each palette states its English name, and an
 * app that is translated installs a namer that looks the id up in its own
 * catalogue.
 *
 * Returning null for an unknown id is the normal case, not an error: the
 * English name is used then. That is what lets a palette be added here without
 * breaking an app that has not been translated yet.
 */
public typealias Trial3PaletteNamer = (String) -> String?

/** The namer in force. Null means every palette shows its English name. */
public val LocalTrial3PaletteNamer: ProvidableCompositionLocal<Trial3PaletteNamer?> =
    staticCompositionLocalOf { null }

/** The name to show for this palette, translated if the app supplied a namer. */
@Composable
public fun Trial3PaletteSpec.displayName(): String =
    LocalTrial3PaletteNamer.current?.invoke(id) ?: defaultName
