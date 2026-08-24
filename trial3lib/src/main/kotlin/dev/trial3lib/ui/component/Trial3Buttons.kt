package dev.trial3lib.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.trial3lib.ui.Trial3
import dev.trial3lib.ui.graphic.Trial3Glyph
import dev.trial3lib.ui.graphic.Trial3GlyphIcon
import dev.trial3lib.ui.token.Alpha
import dev.trial3lib.ui.token.ControlHeight
import dev.trial3lib.ui.token.Stroke
import dev.trial3lib.ui.token.TouchTarget
import dev.trial3lib.ui.token.trial3ControlSpec

/*
 * Buttons.
 *
 * All of them are rectangles with a hairline border, and none of them can round
 * itself back: a Material button reads its own corner token rather than the
 * theme's shape scheme, and that token is a full circle. This file is the reason
 * the app does not look like two apps stitched together.
 *
 * Fixed heights matter more than they look. On a screen where a button is
 * pressed dozens of times in a row, a control that changes size between states
 * is a target that moves under a thumb already travelling towards it.
 */

/**
 * Full-width rectangular button.
 *
 * [quiet] dims the outline and the label without changing the geometry, so a
 * rarely used action can sit next to a common one without competing with it.
 */
@Composable
public fun Trial3WideButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    quiet: Boolean = false,
    enabled: Boolean = true,
    danger: Boolean = false,
    height: Dp = ControlHeight,
) {
    val colors = Trial3.colors
    val ink = if (danger) colors.danger else colors.ink
    val paper = colors.background
    val targetAlpha = (if (enabled) 1f else Alpha.disabled) * (if (quiet) Alpha.quiet else 1f)
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = trial3ControlSpec(),
        label = "wide-button-alpha",
    )
    Trial3ButtonShell(
        onClick = onClick,
        contentColor = (if (filled) paper else ink).copy(alpha = alpha),
        labelStyle = Trial3.typography.labelLarge,
        modifier = modifier,
        enabled = enabled,
        fill = if (filled) ink.copy(alpha = alpha) else Color.Transparent,
        borderColor = ink.copy(alpha = alpha),
        height = height,
        fillMaxWidth = true,
    ) {
        Trial3Text(
            text = label,
            style = Trial3.typography.labelLarge,
            color = (if (filled) paper else ink).copy(alpha = alpha),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/**
 * A button that is only as wide as its label.
 *
 * For a row of two or three actions. Anything that is the single action of a
 * screen should be [Trial3WideButton] instead: a lone small button in the middle of
 * a wide screen is a target the eye has to find.
 */
@Composable
public fun Trial3Button(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    enabled: Boolean = true,
    danger: Boolean = false,
    height: Dp = TouchTarget,
) {
    val colors = Trial3.colors
    val ink = if (danger) colors.danger else colors.ink
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else Alpha.disabled,
        animationSpec = trial3ControlSpec(),
        label = "button-alpha",
    )
    Trial3ButtonShell(
        onClick = onClick,
        contentColor = (if (filled) colors.background else ink).copy(alpha = alpha),
        labelStyle = Trial3.typography.labelLarge,
        modifier = modifier,
        enabled = enabled,
        fill = if (filled) ink.copy(alpha = alpha) else Color.Transparent,
        borderColor = ink.copy(alpha = alpha),
        height = height,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
    ) {
        Trial3Text(
            text = label,
            style = Trial3.typography.labelLarge,
            color = (if (filled) colors.background else ink).copy(alpha = alpha),
            maxLines = 1,
        )
    }
}

/**
 * A text action with no container at all.
 *
 * Replaces a text button, which draws no background either but still reserves a
 * pill-shaped ripple and a minimum corner radius around itself.
 */
@Composable
public fun Trial3TextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Trial3.colors.ink,
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else Alpha.disabled,
        animationSpec = trial3ControlSpec(),
        label = "text-button-alpha",
    )
    Trial3ButtonShell(
        onClick = onClick,
        contentColor = color.copy(alpha = alpha),
        labelStyle = Trial3.typography.labelLarge,
        modifier = modifier,
        enabled = enabled,
        height = TouchTarget,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    ) {
        Trial3Text(
            text = label,
            style = Trial3.typography.labelLarge,
            color = color.copy(alpha = alpha),
            maxLines = 1,
        )
    }
}

/**
 * A glyph you can press.
 *
 * The touch target is 44dp square while the mark inside stays small, so a bar
 * reads as quiet without being hard to hit. Never draw a bare glyph with a
 * clickable on it: an 20dp target at a screen edge loses every argument with the
 * system's own back gesture.
 */
@Composable
public fun Trial3IconButton(
    glyph: Trial3Glyph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = TouchTarget,
    glyphSize: Dp = 20.dp,
    color: Color = Trial3.colors.ink,
    /** The name a screen reader reads out. Every call site should pass one. */
    label: String? = null,
) {
    Trial3IconButtonShell(
        onClick = onClick,
        contentColor = color.copy(alpha = if (enabled) 1f else Alpha.disabled),
        modifier = modifier,
        enabled = enabled,
        size = size,
        label = label,
    ) {
        Trial3GlyphIcon(
            glyph = glyph,
            color = color.copy(alpha = if (enabled) 1f else Alpha.disabled),
            size = glyphSize,
        )
    }
}
