package dev.lattice.ui.token

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Two poles and nothing in between: content is large, service text is small and
 * monospaced.
 *
 * Weights stay a step below the obvious choice. Display text at extra bold in
 * near-black is not emphasis, it is a stain -- the size already carries the
 * emphasis and the weight only adds ink.
 *
 * Nothing here names a font family except the labels, because a font file that
 * does not ship fails the build when it is named. Content therefore renders in
 * the platform sans until a face is chosen, and [LatTypography.withFont] applies
 * a user-installed font to every style at once, including the labels: a font
 * that appears on some screens and not on others reads as a broken app, not as
 * restraint.
 */

private val Mono = FontFamily.Monospace

@Immutable
public data class LatTypography(
    val displayLarge: TextStyle = TextStyle(
        fontSize = 46.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-1.0).sp,
    ),
    val displayMedium: TextStyle = TextStyle(
        fontSize = 38.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.7).sp,
    ),
    val displaySmall: TextStyle = TextStyle(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp,
    ),
    val headlineLarge: TextStyle = TextStyle(
        fontSize = 30.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    val headlineMedium: TextStyle = TextStyle(
        fontSize = 25.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    val headlineSmall: TextStyle = TextStyle(
        fontSize = 20.sp,
        lineHeight = 27.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    val titleLarge: TextStyle = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Normal,
    ),
    val titleMedium: TextStyle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
    ),
    val titleSmall: TextStyle = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
    ),
    val bodyLarge: TextStyle = TextStyle(fontSize = 17.sp, lineHeight = 25.sp),
    val bodyMedium: TextStyle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    val bodySmall: TextStyle = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    val labelLarge: TextStyle = TextStyle(
        fontFamily = Mono,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.1.sp,
    ),
    val labelMedium: TextStyle = TextStyle(
        fontFamily = Mono,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.4.sp,
    ),
    val labelSmall: TextStyle = TextStyle(
        fontFamily = Mono,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.6.sp,
    ),
) {
    /**
     * The same scale in a chosen font.
     *
     * Every style, with no exceptions: the earlier version of this scale in the
     * Ikna app copied the font onto twelve of its fourteen styles, and the two it
     * forgot -- titleLarge and titleSmall -- rendered in the platform face on the
     * screens that used them. That is the kind of gap a list of copy() calls
     * produces and a fold over every field does not.
     */
    public fun withFont(family: FontFamily?): LatTypography {
        if (family == null) return this
        fun TextStyle.f() = this.copy(fontFamily = family)
        return LatTypography(
            displayLarge = displayLarge.f(),
            displayMedium = displayMedium.f(),
            displaySmall = displaySmall.f(),
            headlineLarge = headlineLarge.f(),
            headlineMedium = headlineMedium.f(),
            headlineSmall = headlineSmall.f(),
            titleLarge = titleLarge.f(),
            titleMedium = titleMedium.f(),
            titleSmall = titleSmall.f(),
            bodyLarge = bodyLarge.f(),
            bodyMedium = bodyMedium.f(),
            bodySmall = bodySmall.f(),
            labelLarge = labelLarge.f(),
            labelMedium = labelMedium.f(),
            labelSmall = labelSmall.f(),
        )
    }

    /** Every style, for tests and for a type specimen screen. */
    public fun all(): List<Pair<String, TextStyle>> = listOf(
        "displayLarge" to displayLarge,
        "displayMedium" to displayMedium,
        "displaySmall" to displaySmall,
        "headlineLarge" to headlineLarge,
        "headlineMedium" to headlineMedium,
        "headlineSmall" to headlineSmall,
        "titleLarge" to titleLarge,
        "titleMedium" to titleMedium,
        "titleSmall" to titleSmall,
        "bodyLarge" to bodyLarge,
        "bodyMedium" to bodyMedium,
        "bodySmall" to bodySmall,
        "labelLarge" to labelLarge,
        "labelMedium" to labelMedium,
        "labelSmall" to labelSmall,
    )
}
