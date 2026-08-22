package dev.lattice.ui.token

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.Locale
import kotlin.math.pow

/*
 * Contrast arithmetic.
 *
 * It sits in the token package rather than in the theme because three unrelated
 * callers need it: a palette asserts its own readability in a unit test, a
 * custom-colour editor has to say "this pair is unreadable" while the user is
 * still typing, and the system bars have to choose black or white icons from the
 * background colour actually being painted rather than from the phone's dark
 * mode switch. Those two stop agreeing the moment an app owns its own light and
 * dark setting, which is how you get white status icons on a white screen.
 *
 * Pure Kotlin on purpose: android.graphics.Color is not available in a unit
 * test, and every rule here is asserted by one.
 */

/** WCAG 2.1 relative luminance, 0 (black) to 1 (white). */
public fun relativeLuminance(color: Color): Double {
    fun channel(value: Float): Double {
        val c = value.toDouble()
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(color.red) +
        0.7152 * channel(color.green) +
        0.0722 * channel(color.blue)
}

/** WCAG contrast ratio: 1.0 for two identical colours, 21.0 for black on white. */
public fun contrastRatio(a: Color, b: Color): Double {
    val la = relativeLuminance(a)
    val lb = relativeLuminance(b)
    val hi = if (la > lb) la else lb
    val lo = if (la > lb) lb else la
    return (hi + 0.05) / (lo + 0.05)
}

/** The line below which body text stops being readable. */
public const val MIN_READABLE_CONTRAST: Double = 4.5

/**
 * Whether a background needs dark icons drawn on it.
 *
 * The threshold sits above 0.5 on purpose: mid-tones are ambiguous, and a wrong
 * guess costs an invisible status bar, so the darker half of the ambiguous range
 * keeps light icons.
 */
public fun isLightColor(color: Color): Boolean = relativeLuminance(color) > 0.45

public fun ratioText(ratio: Double): String = String.format(Locale.US, "%.1f:1", ratio)

/**
 * "#1B1813", "1b1813" -> Color. Null for anything that is not six hex digits, so
 * a half-typed value simply does not apply yet instead of throwing.
 */
public fun parseHexColor(text: String): Color? {
    val body = text.trim().removePrefix("#")
    if (body.length != 6) return null
    if (!body.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
    val value = body.toLongOrNull(16) ?: return null
    return Color((0xFF000000L or value).toInt())
}

/** Six upper-case hex digits, no leading hash. */
public fun hexOf(color: Color): String =
    String.format(Locale.US, "%06X", color.toArgb() and 0xFFFFFF)

/** Hue in degrees (0..360) and saturation (0..1), computed rather than platform. */
public fun hueAndSaturation(color: Color): Pair<Float, Float> {
    val r = color.red
    val g = color.green
    val b = color.blue
    val high = maxOf(r, g, b)
    val low = minOf(r, g, b)
    val span = high - low
    if (span <= 0f || high <= 0f) return 0f to 0f
    val raw = when (high) {
        r -> 60f * ((g - b) / span)
        g -> 60f * ((b - r) / span + 2f)
        else -> 60f * ((r - g) / span + 4f)
    }
    val hue = ((raw % 360f) + 360f) % 360f
    return hue to (span / high)
}

/** Linear mix of two opaque colours. Kept internal to the token package. */
internal fun mixColors(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = 1f,
)
