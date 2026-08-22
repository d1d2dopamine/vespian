package dev.lattice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import dev.lattice.ui.token.Alpha
import dev.lattice.ui.token.LatDarkPalette
import dev.lattice.ui.token.LatPalette
import dev.lattice.ui.token.LatTypography
import dev.lattice.ui.token.LocalLatMotionEnabled
import dev.lattice.ui.token.dangerFor

/*
 * The theme.
 *
 * Two defaults used to override the whole palette in the app this library was
 * extracted from, and both are handled here once instead of at every call site.
 *
 * Nothing in a Compose tree paints the window by itself. With no Surface
 * anywhere -- deliberately, it is a Material component -- what showed through was
 * the Android window background from the platform theme, a warm dark grey: the
 * light theme was computed correctly and then never reached the screen. And the
 * content colour falls back to black when nothing provides it, so every piece of
 * text that did not name a colour rendered black on both lightings.
 *
 * So this composable paints the background and provides the content colour, the
 * type scale and the motion policy. A screen that forgets to name a colour still
 * comes out in ink.
 */

/**
 * The colours a screen actually asks for, by role rather than by hue.
 *
 * Derived from a four-colour palette, never authored directly. The names are the
 * vocabulary of the design: paper, ink, a quieter ink, a line, one accent, and a
 * colour that only destructive controls are allowed to use.
 */
@Immutable
public data class LatColors(
    /** The page. */
    val background: Color,
    /** A surface one step away from the page. Not a card and not elevated. */
    val panel: Color,
    /** Text, marks, borders of controls. */
    val ink: Color,
    /** Secondary text: captions, units, explanations. */
    val muted: Color,
    /** Rules and field borders. */
    val line: Color,
    /** The single coloured thing on the screen. */
    val accent: Color,
    /** Text drawn on top of the accent. */
    val onAccent: Color,
    /** Destructive controls only. */
    val danger: Color,
    /** Text drawn on top of danger. */
    val onDanger: Color,
    /** True when this is the light lighting: status bar icons read this. */
    val light: Boolean,
)

/** The colour set a palette resolves to. */
public fun LatPalette.toColors(): LatColors = LatColors(
    background = background,
    panel = panel,
    ink = ink,
    muted = muted,
    line = line,
    accent = accent,
    onAccent = background,
    danger = dangerFor(this),
    onDanger = background,
    light = light,
)

public val LocalLatColors = staticCompositionLocalOf { LatDarkPalette.toColors() }

public val LocalLatTypography = staticCompositionLocalOf { LatTypography() }

/** The palette itself, for the rare caller that needs the four source colours. */
public val LocalLatPalette = staticCompositionLocalOf { LatDarkPalette }

/**
 * The colour text and marks take when they do not name one.
 *
 * compositionLocalOf rather than static: it changes inside a filled button, and
 * only the text inside that button should recompose.
 */
public val LocalContentColor = compositionLocalOf { Color.Black }

/** The style text takes when it does not name one. */
public val LocalLatTextStyle = compositionLocalOf { TextStyle.Default }

/**
 * Everything a screen reads from the theme.
 *
 * `Lattice.colors.muted` instead of `MaterialTheme.colorScheme.onSurfaceVariant`:
 * the slot is named after what it is for, not after the component it was
 * invented for.
 */
public object Lattice {
    public val colors: LatColors
        @Composable @ReadOnlyComposable get() = LocalLatColors.current

    public val typography: LatTypography
        @Composable @ReadOnlyComposable get() = LocalLatTypography.current

    public val palette: LatPalette
        @Composable @ReadOnlyComposable get() = LocalLatPalette.current

    public val motionEnabled: Boolean
        @Composable @ReadOnlyComposable get() = LocalLatMotionEnabled.current

    /** The opacity a disabled control is drawn at. */
    public val disabledAlpha: Float get() = Alpha.disabled
}

/**
 * Wrap the whole app in this, once.
 *
 * @param palette the four colours, already resolved for the current lighting.
 * @param contentFont a font the user installed, or null for the platform face.
 *   Applied to every style including the mono labels: a font that appears on some
 *   screens and not others reads as a broken app, not as restraint.
 * @param motionEnabled the app's own animation setting. Every control in this
 *   library reads it, so nothing has to be told twice and nothing keeps moving
 *   after the user asked it not to.
 * @param paintBackground false only when the caller has already painted the
 *   window, for example inside a dialog.
 */
@Composable
public fun LatticeTheme(
    palette: LatPalette = LatDarkPalette,
    contentFont: FontFamily? = null,
    motionEnabled: Boolean = true,
    paintBackground: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = palette.toColors()
    val typography = LatTypography().withFont(contentFont)
    CompositionLocalProvider(
        LocalLatColors provides colors,
        LocalLatPalette provides palette,
        LocalLatTypography provides typography,
        LocalContentColor provides colors.ink,
        LocalLatTextStyle provides typography.bodyMedium,
        LocalLatMotionEnabled provides motionEnabled,
    ) {
        if (paintBackground) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background),
            ) {
                content()
            }
        } else {
            content()
        }
    }
}
