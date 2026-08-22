package dev.lattice.ui.component

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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
import dev.lattice.ui.Lattice
import dev.lattice.ui.graphic.LatGlyph
import dev.lattice.ui.graphic.LatGlyphIcon
import dev.lattice.ui.token.Alpha
import dev.lattice.ui.token.BarHeight
import dev.lattice.ui.token.Edge
import dev.lattice.ui.token.Space
import dev.lattice.ui.token.Stroke
import dev.lattice.ui.token.TouchTarget
import dev.lattice.ui.token.latControlSpec

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
public fun LatTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    height: Dp = BarHeight,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = Edge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Box(Modifier.width(Space.sm))
            }
            Column(modifier = Modifier.weight(1f)) {
                LatText(
                    text = title,
                    style = Lattice.typography.titleMedium,
                    color = Lattice.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    LatText(
                        text = subtitle,
                        style = Lattice.typography.labelSmall,
                        color = Lattice.colors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) trailing()
        }
        LatRule()
    }
}

/**
 * The bar the thumb actually reaches.
 *
 * Fixed height, a rule above it, no shadow. Put two to five [LatNavItem]s in it;
 * past five, the labels stop fitting and the targets stop being distinguishable
 * by feel.
 */
@Composable
public fun LatBottomBar(
    modifier: Modifier = Modifier,
    height: Dp = BarHeight,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LatRule()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(Lattice.colors.background),
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
public fun RowScope.LatNavItem(
    glyph: LatGlyph,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Lattice.colors
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else Alpha.quiet,
        animationSpec = latControlSpec(),
        label = "nav-item-alpha",
    )
    Column(
        modifier = modifier
            .weight(1f)
            .height(BarHeight)
            .semantics {
                role = Role.Tab
                this.selected = selected
            }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LatGlyphIcon(
            glyph = glyph,
            color = colors.ink.copy(alpha = alpha),
            size = 20.dp,
            label = label,
        )
        Box(Modifier.height(Space.xs))
        LatText(
            text = label,
            style = Lattice.typography.labelSmall,
            color = colors.ink.copy(alpha = alpha),
            maxLines = 1,
        )
        Box(Modifier.height(Space.xs))
        Box(
            modifier = Modifier
                .width(if (selected) 18.dp else 0.dp)
                .height(Stroke.heavy)
                .background(colors.accent),
        )
    }
}

/**
 * Tabs inside a screen, as opposed to destinations across it.
 *
 * A filled block marks the selected one -- the same rule as [LatChip], because a
 * user should not have to learn two vocabularies for "this one".
 */
@Composable
public fun LatTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Lattice.colors
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
                LatText(
                    text = label,
                    style = Lattice.typography.labelMedium,
                    color = if (isSelected) colors.background else colors.muted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            if (index != tabs.lastIndex) LatVerticalRule()
        }
    }
}

/**
 * A screen: an optional top bar, the content, an optional bottom bar.
 *
 * The replacement for Material's Scaffold, minus the parts this design has no
 * use for -- no floating action button, no snackbar host (see [LatNotice]), no
 * elevation. The content receives the space that is left, and nothing is drawn
 * underneath the bars, so nothing has to be padded around them by hand.
 */
@Composable
public fun LatScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Lattice.colors.background),
    ) {
        if (topBar != null) topBar()
        Column(modifier = Modifier.weight(1f)) { content() }
        if (bottomBar != null) bottomBar()
    }
}
