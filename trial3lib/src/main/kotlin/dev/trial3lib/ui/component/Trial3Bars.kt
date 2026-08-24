package dev.trial3lib.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.trial3lib.ui.LocalContentColor
import dev.trial3lib.ui.Trial3
import dev.trial3lib.ui.graphic.Trial3Glyph
import dev.trial3lib.ui.graphic.Trial3GlyphIcon
import dev.trial3lib.ui.token.Alpha
import dev.trial3lib.ui.token.BarHeight
import dev.trial3lib.ui.token.Edge
import dev.trial3lib.ui.token.Space
import dev.trial3lib.ui.token.Stroke
import dev.trial3lib.ui.token.TouchTarget
import dev.trial3lib.ui.token.trial3ControlSpec

/*
 * Bars.
 *
 * One rule decides the whole file: the way out is at the bottom.
 *
 * Navigation is the most-used control in an app, a phone is held from below, and
 * a back arrow in the top-left corner is a reach across the entire screen that
 * also moves from screen to screen depending on what the top bar happens to
 * contain. So the bottom bar is where navigation lives, the top bar is a label
 * rather than a control strip, and neither of them is elevated -- they are
 * separated from the content by a rule, which is what a rule is for.
 */

/**
 * The screen's name, and at most one action.
 *
 * Not a control strip. A top bar that accumulates four icons becomes the place
 * where every feature nobody could place goes to hide.
 */
@Composable
public fun Trial3TopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    height: Dp = BarHeight,
) {
    Trial3TopBarShell(
        modifier = modifier,
        height = height,
        leading = leading,
        trailing = trailing,
    ) {
        Trial3Text(
            text = title,
            style = Trial3.typography.titleMedium,
            color = Trial3.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Trial3Text(
                text = subtitle,
                style = Trial3.typography.labelSmall,
                color = Trial3.colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The bar the thumb actually reaches.
 *
 * Fixed height, a rule above it, no shadow. Put two to five [Trial3NavItem]s in it;
 * past five, the labels stop fitting and the targets stop being distinguishable
 * by feel.
 */
@Composable
public fun Trial3BottomBar(
    modifier: Modifier = Modifier,
    height: Dp = BarHeight,
    containerColor: Color = Trial3.colors.background,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Trial3Rule()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(containerColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = content,
        )
    }
}

/**
 * One destination in the bottom bar.
 *
 * Selection is carried by opacity and by a filled bar under the label, never by
 * hue alone: on a palette whose accent is the same family as its ink, a coloured
 * icon and a grey icon are the same icon.
 */
@Composable
public fun RowScope.Trial3NavItem(
    glyph: Trial3Glyph,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Trial3.colors
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else Alpha.quiet,
        animationSpec = trial3ControlSpec(),
        label = "nav-item-alpha",
    )
    Trial3NavItemShell(
        selected = selected,
        onClick = onClick,
        contentColor = colors.ink.copy(alpha = alpha),
        labelStyle = Trial3.typography.labelSmall,
        modifier = modifier,
        label = {
            Trial3Text(
                text = label,
                style = Trial3.typography.labelSmall,
                color = colors.ink.copy(alpha = alpha),
                maxLines = 1,
            )
        },
        icon = {
            Trial3GlyphIcon(
                glyph = glyph,
                color = colors.ink.copy(alpha = alpha),
                size = 20.dp,
                label = label,
            )
        },
    )
}

/**
 * Tabs inside a screen, as opposed to destinations across it.
 *
 * A filled block marks the selected one -- the same rule as [Trial3Chip], because a
 * user should not have to learn two vocabularies for "this one".
 */
@Composable
public fun Trial3Tabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Trial3.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TouchTarget),
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(if (isSelected) colors.ink else Color.Transparent)
                    .semantics {
                        role = Role.Tab
                        selected = isSelected
                    }
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Trial3Text(
                    text = label,
                    style = Trial3.typography.labelMedium,
                    color = if (isSelected) colors.background else colors.muted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            if (index != tabs.lastIndex) Trial3VerticalRule()
        }
    }
}

/**
 * A screen: an optional top bar, the content, an optional bottom bar.
 *
 * The replacement for Material's Scaffold, minus the parts this design has no
 * use for -- no floating action button, no snackbar host (see [Trial3Notice]), no
 * elevation. The content receives the space that is left, and nothing is drawn
 * underneath the bars, so nothing has to be padded around them by hand.
 */
@Composable
public fun Trial3Scaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    containerColor: Color = Trial3.colors.background,
    contentColor: Color = Trial3.colors.ink,
    applySystemBars: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    var shell = modifier
        .fillMaxSize()
        .background(containerColor)
    if (applySystemBars) shell = shell.systemBarsPadding()
    Column(modifier = shell) {
        if (topBar != null) topBar()
        val weighted = Modifier.weight(1f)
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Column(modifier = weighted, content = content)
        }
        if (bottomBar != null) bottomBar()
    }
}
