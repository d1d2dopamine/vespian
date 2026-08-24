package dev.trial3lib.ui.compat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import dev.trial3lib.ui.Trial3
import dev.trial3lib.ui.LocalTrial3TextStyle
import dev.trial3lib.ui.component.Trial3Text
import dev.trial3lib.ui.token.Trial3Typography

/*
 * The bridge.
 *
 * This exists so an app can drop Material 3 in one commit instead of one screen
 * at a time. Every call site keeps writing MaterialTheme.colorScheme.onSurfaceVariant
 * and Text(...), the imports change from androidx.compose.material3 to this
 * package, and the pixels stay where they were.
 *
 * It is scaffolding, not architecture. The mapping below is deliberately narrow:
 * only the slots a real app actually reads are here, so anything unmapped fails
 * to compile rather than silently rendering in a colour nobody chose. Migrate
 * away from this package screen by screen and delete it when the last import is
 * gone.
 */

/** Material's colour-slot names, answered from the Trial3 palette. */
public class CompatColorScheme internal constructor() {
    /** The accent. The one colour that is allowed to be a colour. */
    public val primary: Color @Composable @ReadOnlyComposable get() = Trial3.colors.accent
    public val secondary: Color @Composable @ReadOnlyComposable get() = Trial3.colors.accent
    public val tertiary: Color @Composable @ReadOnlyComposable get() = Trial3.colors.accent

    /** The page. Not a tonal surface: the actual background of the app. */
    public val background: Color @Composable @ReadOnlyComposable get() = Trial3.colors.background

    /**
     * Anything Material would raise off the page is drawn flat here, so surface
     * and surfaceVariant both resolve to the panel tone -- a barely-mixed shade of
     * the background, never a grey.
     */
    public val surface: Color @Composable @ReadOnlyComposable get() = Trial3.colors.panel
    public val surfaceVariant: Color @Composable @ReadOnlyComposable get() = Trial3.colors.panel
    public val surfaceContainer: Color @Composable @ReadOnlyComposable get() = Trial3.colors.panel
    public val surfaceContainerHigh: Color @Composable @ReadOnlyComposable get() = Trial3.colors.panel
    public val surfaceContainerLow: Color @Composable @ReadOnlyComposable get() = Trial3.colors.panel

    /** Body text. */
    public val onBackground: Color @Composable @ReadOnlyComposable get() = Trial3.colors.ink
    public val onSurface: Color @Composable @ReadOnlyComposable get() = Trial3.colors.ink

    /**
     * Secondary text. This is the single most-used slot in a flat interface,
     * because everything that is not the sentence you are reading is quieter.
     */
    public val onSurfaceVariant: Color @Composable @ReadOnlyComposable get() = Trial3.colors.muted

    /** Text on top of the accent, and on top of anything filled with ink. */
    public val onPrimary: Color @Composable @ReadOnlyComposable get() = Trial3.colors.onAccent
    public val onSecondary: Color @Composable @ReadOnlyComposable get() = Trial3.colors.onAccent
    public val onTertiary: Color @Composable @ReadOnlyComposable get() = Trial3.colors.onAccent

    /** Rules and borders. Both outline slots are the same hairline. */
    public val outline: Color @Composable @ReadOnlyComposable get() = Trial3.colors.line
    public val outlineVariant: Color @Composable @ReadOnlyComposable get() = Trial3.colors.line

    /**
     * Destructive actions. Contrast-checked against the current background, so it
     * is never the raw red that vanishes on a red palette.
     */
    public val error: Color @Composable @ReadOnlyComposable get() = Trial3.colors.danger
    public val onError: Color @Composable @ReadOnlyComposable get() = Trial3.colors.onDanger
    public val errorContainer: Color @Composable @ReadOnlyComposable get() = Trial3.colors.background
    public val onErrorContainer: Color @Composable @ReadOnlyComposable get() = Trial3.colors.danger

    /** Never used for a scrim over content; here so old call sites compile. */
    public val scrim: Color @Composable @ReadOnlyComposable get() = Trial3.colors.background
    public val inverseSurface: Color @Composable @ReadOnlyComposable get() = Trial3.colors.ink
    public val inverseOnSurface: Color @Composable @ReadOnlyComposable get() = Trial3.colors.background
}

/** Every shape is a rectangle. That is the whole point of the library. */
public class CompatShapes internal constructor() {
    public val extraSmall: Shape get() = RectangleShape
    public val small: Shape get() = RectangleShape
    public val medium: Shape get() = RectangleShape
    public val large: Shape get() = RectangleShape
    public val extraLarge: Shape get() = RectangleShape
}

/**
 * A stand-in for Material's theme object.
 *
 * The names are Material's; the values come from [Trial3]. Nothing here reads a
 * Material composition local, so an app can use this with no Material artifact
 * on the classpath at all.
 */
public object MaterialTheme {
    private val scheme = CompatColorScheme()
    private val shapeSet = CompatShapes()

    public val colorScheme: CompatColorScheme
        @Composable @ReadOnlyComposable get() = scheme

    public val typography: Trial3Typography
        @Composable @ReadOnlyComposable get() = Trial3.typography

    public val shapes: CompatShapes
        @Composable @ReadOnlyComposable get() = shapeSet
}

/**
 * Material's Text, with the same parameter list, drawn by [Trial3Text].
 *
 * Signature-compatible on purpose: an app with hundreds of Text(...) call sites
 * changes an import and nothing else.
 */
@Composable
public fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle? = null,
): Unit = Trial3Text(
    text = text,
    modifier = modifier,
    color = color,
    fontSize = fontSize,
    fontStyle = fontStyle,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
    letterSpacing = letterSpacing,
    textDecoration = textDecoration,
    textAlign = textAlign,
    lineHeight = lineHeight,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    minLines = minLines,
    onTextLayout = onTextLayout,
    // Material's Text has no style parameter that means "whatever the
    // surrounding block decided"; it has a non-null default. Here the
    // default is null and resolves to the ambient style, so a Text inside
    // a button or a bar inherits that block's type instead of overriding
    // it. Trial3Text wants a real style, so it is resolved here.
    style = style ?: LocalTrial3TextStyle.current,
)

@Composable
public fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle? = null,
): Unit = Trial3Text(
    text = text,
    modifier = modifier,
    color = color,
    fontSize = fontSize,
    fontStyle = fontStyle,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
    letterSpacing = letterSpacing,
    textDecoration = textDecoration,
    textAlign = textAlign,
    lineHeight = lineHeight,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    minLines = minLines,
    onTextLayout = onTextLayout,
    // Material's Text has no style parameter that means "whatever the
    // surrounding block decided"; it has a non-null default. Here the
    // default is null and resolves to the ambient style, so a Text inside
    // a button or a bar inherits that block's type instead of overriding
    // it. Trial3Text wants a real style, so it is resolved here.
    style = style ?: LocalTrial3TextStyle.current,
)
