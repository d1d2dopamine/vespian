package dev.trial3lib.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.trial3lib.ui.LocalContentColor
import dev.trial3lib.ui.LocalTrial3TextStyle
import dev.trial3lib.ui.Trial3
import dev.trial3lib.ui.token.BarHeight
import dev.trial3lib.ui.token.Edge
import dev.trial3lib.ui.token.Space
import dev.trial3lib.ui.token.Stroke
import dev.trial3lib.ui.token.TouchTarget
import dev.trial3lib.ui.token.Trial3Shape
import dev.trial3lib.ui.token.readable

/*
 * The geometry, once.
 *
 * Every public component in this library has two callers: a screen that names a
 * label (`Trial3Button(label = "OK")`) and a screen that has not been rewritten
 * yet and still passes a content slot (`Button(onClick) { Text("OK") }`, answered
 * by the compat package). Before this file existed both were drawn separately,
 * so the library shipped two switches, two panels, two bars and two dialogs, and
 * the app got whichever set its import happened to name. A tumbler fixed in one
 * of them stayed broken in the other.
 *
 * So the shape lives here and nowhere else. The public components decide colour,
 * state and wording; these functions decide size, border, padding, alignment and
 * what a screen reader is told. Change a control's geometry once, and every
 * caller -- new API and shim alike -- changes with it.
 *
 * Everything here is internal on purpose: it is the library's own vocabulary,
 * not a second public API to keep compatible.
 */

/**
 * The shape of everything pressable that holds a label.
 *
 * [height] pins an exact height (a control whose size changes between states is
 * a target that moves under a thumb already travelling towards it); when it is
 * null the row is at least [minHeight] tall and wraps its content.
 */
@Composable
internal fun Trial3ButtonShell(
    onClick: () -> Unit,
    contentColor: Color,
    labelStyle: TextStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fill: Color = Color.Transparent,
    borderColor: Color? = null,
    height: Dp? = null,
    minHeight: Dp = TouchTarget,
    fillMaxWidth: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
    content: @Composable RowScope.() -> Unit,
) {
    var shell = modifier
    if (fillMaxWidth) shell = shell.fillMaxWidth()
    shell = if (height != null) {
        shell.height(height)
    } else {
        shell.defaultMinSize(minHeight = minHeight)
    }
    shell = shell.background(fill, Trial3Shape.square)
    if (borderColor != null) {
        shell = shell.border(Stroke.hair, borderColor, Trial3Shape.square)
    }
    Row(
        modifier = shell
            .semantics { role = Role.Button }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val scope = this
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTrial3TextStyle provides labelStyle,
        ) {
            scope.content()
        }
    }
}

/**
 * The shape of a tappable square holding one mark.
 *
 * The target is [size] while the mark inside stays small, so a bar reads as
 * quiet without being hard to hit.
 */
@Composable
internal fun Trial3IconButtonShell(
    onClick: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = TouchTarget,
    label: String? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .semantics {
                role = Role.Button
                if (label != null) contentDescription = label
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

/**
 * The shape of the screen's name strip: one row, a rule under it, no shadow.
 *
 * The rule is part of the bar rather than something a screen remembers to add,
 * which is why a top bar cannot arrive without its separator.
 */
@Composable
internal fun Trial3TopBarShell(
    modifier: Modifier = Modifier,
    height: Dp = BarHeight,
    horizontalPadding: Dp = Edge,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    title: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().background(Trial3.colors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(Space.sm))
            }
            Column(modifier = Modifier.weight(1f), content = title)
            if (trailing != null) trailing()
        }
        Trial3Rule()
    }
}

/**
 * The shape of one destination in the bottom bar.
 *
 * Selection is carried by opacity and by a short filled bar under the label,
 * never by hue alone: on a palette whose accent is the same family as its ink, a
 * coloured icon and a quiet icon are the same icon.
 */
@Composable
internal fun RowScope.Trial3NavItemShell(
    selected: Boolean,
    onClick: () -> Unit,
    contentColor: Color,
    labelStyle: TextStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = BarHeight,
    markWidth: Dp = 18.dp,
    label: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    val isSelected = selected
    Column(
        modifier = modifier
            .weight(1f)
            .height(height)
            .semantics {
                role = Role.Tab
                this.selected = isSelected
            }
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTrial3TextStyle provides labelStyle,
        ) {
            icon()
            if (label != null) {
                Spacer(Modifier.height(Space.xs))
                label()
            }
        }
        Spacer(Modifier.height(Space.xs))
        Box(
            modifier = Modifier
                .width(if (isSelected) markWidth else 0.dp)
                .height(Stroke.heavy)
                .background(Trial3.colors.accent),
        )
    }
}

/**
 * The shape of anything typed into: a rectangle with a hairline border and no
 * floating label, so focus never changes the height of the row.
 */
@Composable
internal fun Trial3FieldShell(
    modifier: Modifier = Modifier,
    height: Dp? = null,
    borderColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = Space.md, vertical = 0.dp),
    content: @Composable () -> Unit,
) {
    var shell = modifier
    if (height != null) shell = shell.height(height)
    Box(
        modifier = shell
            .border(Stroke.hair, borderColor ?: Trial3.colors.line, Trial3Shape.square)
            .padding(contentPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}

/**
 * The shape of a question that has to be answered before anything else happens.
 *
 * Square, bordered, painted in the panel tone, and capped at the reading width
 * so it does not stretch across a tablet.
 */
@Composable
internal fun Trial3DialogShell(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = Trial3.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .readable()
                .background(colors.panel)
                .border(Stroke.hair, colors.line, Trial3Shape.square)
                .padding(Edge),
            verticalArrangement = Arrangement.spacedBy(Space.md),
            content = content,
        )
    }
}
