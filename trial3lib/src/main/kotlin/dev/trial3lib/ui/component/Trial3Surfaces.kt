package dev.trial3lib.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.trial3lib.ui.Trial3
import dev.trial3lib.ui.token.Alpha
import dev.trial3lib.ui.token.Edge
import dev.trial3lib.ui.token.Space
import dev.trial3lib.ui.token.Stroke
import dev.trial3lib.ui.token.TouchTarget

/*
 * Surfaces.
 *
 * There is no elevation in this library and no shadow anywhere. A rule separates
 * things; a border encloses them. Those are the only two devices, which is why a
 * screen made of them reads as one object instead of as a pile of cards.
 */

/** The page. Paints the background and applies the screen margin. */
@Composable
public fun Trial3Screen(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = Edge,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Trial3.colors.background)
            .padding(horizontal = horizontalPadding),
        content = content,
    )
}

/** A rule. Replaces every shadow and every card edge. */
@Composable
public fun Trial3Rule(
    modifier: Modifier = Modifier,
    thickness: Dp = Stroke.hair,
    color: Color = Trial3.colors.line,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color),
    )
}

/** A vertical rule, for a row of figures that need separating. */
@Composable
public fun Trial3VerticalRule(
    modifier: Modifier = Modifier,
    thickness: Dp = Stroke.hair,
    color: Color = Trial3.colors.line,
) {
    Box(
        modifier = modifier
            .width(thickness)
            .background(color),
    )
}

/**
 * A bordered block for text the app is telling you, as opposed to text you are
 * meant to act on.
 *
 * Explanations otherwise run down the screen as loose paragraphs with a hairline
 * somewhere in the middle, which reads as one long wall: nothing says where the
 * explaining stops and the choosing begins. The border says it. It is heavier
 * than the hairline used for buttons and fields on purpose, and it is the muted
 * colour rather than a grey, so it stays correct in every palette and lighting.
 */
@Composable
public fun Trial3Panel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(Stroke.heavy, Trial3.colors.muted.copy(alpha = Alpha.panelBorder))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
        content = content,
    )
}

/**
 * A block of related content, optionally pressable.
 *
 * This is what a card would be if a card were allowed. No corner radius, no
 * elevation, no tonal fill: a hairline border and the page behind it. [filled]
 * uses the panel colour for the rare case where two blocks sit side by side and
 * a border alone does not separate them.
 */
@Composable
public fun Trial3Block(
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    bordered: Boolean = true,
    onClick: (() -> Unit)? = null,
    padding: Dp = Space.md,
    spacing: Dp = Space.sm,
    fillColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = Trial3.colors
    var shell = modifier.fillMaxWidth()
    if (filled) shell = shell.background(fillColor ?: colors.panel)
    if (bordered) shell = shell.border(Stroke.hair, borderColor ?: colors.line)
    if (onClick != null) shell = shell.clickable(onClick = onClick)
    Column(
        modifier = shell.padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

/**
 * A section mark: small mono capitals over the content it introduces.
 *
 * Not a heading. A heading is content; this is furniture, and it is drawn in the
 * muted colour at label size so it never competes with the first line under it.
 */
@Composable
public fun Trial3SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Trial3.colors.muted,
) {
    Trial3Text(
        text = text,
        modifier = modifier,
        style = Trial3.typography.labelMedium,
        color = color,
        maxLines = 1,
    )
}

/**
 * One line of a list: a title, an optional second line, and whatever sits on the
 * right.
 *
 * Fixed minimum height, because a list whose rows change height as their
 * subtitles arrive is a list that moves under a thumb already travelling towards
 * one of them.
 */
@Composable
public fun Trial3Row(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = Trial3.colors
    val alpha = if (enabled) 1f else Alpha.disabled
    var shell = modifier
        .fillMaxWidth()
        .heightIn(min = TouchTarget)
    if (onClick != null) shell = shell.clickable(enabled = enabled, onClick = onClick)
    Row(
        modifier = shell.padding(vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Trial3Text(
                text = title,
                style = Trial3.typography.bodyLarge,
                color = colors.ink.copy(alpha = alpha),
            )
            if (subtitle != null) {
                Spacer(Modifier.height(Space.xs))
                Trial3Text(
                    text = subtitle,
                    style = Trial3.typography.bodySmall,
                    color = colors.muted.copy(alpha = alpha),
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(Space.md))
            trailing()
        }
    }
}

/**
 * A figure and what it is a figure of.
 *
 * The number is display-sized and the caption is a mono label under it, which is
 * the one arrangement that survives being read at arm's length.
 */
@Composable
public fun Trial3Figure(
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    color: Color = Trial3.colors.ink,
) {
    Column(modifier = modifier) {
        Trial3Text(
            text = value,
            style = Trial3.typography.displaySmall,
            color = color,
            maxLines = 1,
        )
        Spacer(Modifier.height(Space.xs))
        Trial3SectionLabel(text = caption)
    }
}
