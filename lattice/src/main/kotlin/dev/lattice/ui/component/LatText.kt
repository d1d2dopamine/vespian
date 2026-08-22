package dev.lattice.ui.component

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import dev.lattice.ui.LocalContentColor
import dev.lattice.ui.LocalLatTextStyle

/*
 * Text.
 *
 * A thin merge over BasicText, which is the foundation primitive Material's own
 * Text is also built on. The reason to have this at all rather than call
 * BasicText directly: BasicText takes no colour parameter worth using and no
 * ambient style, so every call site would have to name both, and the day someone
 * forgets is the day a label renders black on a black field.
 *
 * The parameter list matches Material 3's Text deliberately, in the same order
 * and with the same defaults, so replacing one with the other is an import
 * change and nothing else.
 */
@Composable
public fun LatText(
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
    style: TextStyle = LocalLatTextStyle.current,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = mergedStyle(
            style, color, fontSize, fontStyle, fontWeight, fontFamily,
            letterSpacing, textDecoration, textAlign, lineHeight,
        ),
        onTextLayout = onTextLayout,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
    )
}

/** The same, for text that carries its own spans. */
@Composable
public fun LatText(
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
    style: TextStyle = LocalLatTextStyle.current,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = mergedStyle(
            style, color, fontSize, fontStyle, fontWeight, fontFamily,
            letterSpacing, textDecoration, textAlign, lineHeight,
        ),
        onTextLayout = onTextLayout,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
    )
}

/**
 * The colour resolution rule, in one place: an explicit colour wins, then a
 * colour already in the style, then the ambient content colour. That last step
 * is the whole reason this function exists -- it is what makes an unnamed colour
 * correct instead of black.
 */
@Composable
private fun mergedStyle(
    style: TextStyle,
    color: Color,
    fontSize: TextUnit,
    fontStyle: FontStyle?,
    fontWeight: FontWeight?,
    fontFamily: FontFamily?,
    letterSpacing: TextUnit,
    textDecoration: TextDecoration?,
    textAlign: TextAlign?,
    lineHeight: TextUnit,
): TextStyle {
    val ambient = LocalContentColor.current
    val resolved = when {
        color != Color.Unspecified -> color
        style.color != Color.Unspecified -> style.color
        else -> ambient
    }
    return style.merge(
        TextStyle(
            color = resolved,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
        ),
    )
}
