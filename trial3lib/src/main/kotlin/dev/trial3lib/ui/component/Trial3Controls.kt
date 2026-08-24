package dev.trial3lib.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.trial3lib.ui.Trial3
import dev.trial3lib.ui.token.Alpha
import dev.trial3lib.ui.token.SmallControlHeight
import dev.trial3lib.ui.token.Space
import dev.trial3lib.ui.token.Stroke
import dev.trial3lib.ui.token.TouchTarget
import dev.trial3lib.ui.token.trial3ControlSpec
import kotlin.math.roundToInt

/*
 * Controls.
 *
 * A switch made of two rectangles, a chip that fills instead of tinting, a box
 * that is checked by being filled, a slider whose knob is a square. None of them
 * can round itself, none of them ripples, and every one of them reads the app's
 * motion setting rather than inventing its own.
 *
 * Everything here announces itself properly to a screen reader. A drawn shape
 * has no text in it, so a control made of rectangles is a control that says
 * nothing unless it is told to.
 */

/**
 * A switch: a filled block that travels the 24dp between its two unambiguous end
 * states. It never bounces and snaps immediately when motion is off.
 */
@Composable
public fun Trial3Toggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    /**
     * A name for the switch itself. Only needed where the row around it does not
     * already say what is being switched.
     */
    label: String? = null,
) {
    val ink = Trial3.colors.ink
    val alpha = if (enabled) 1f else Alpha.disabled
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = trial3ControlSpec(),
        label = "toggle-position",
    )
    val knobColor by animateColorAsState(
        targetValue = if (checked) ink.copy(alpha = alpha) else ink.copy(alpha = Alpha.trackOff * alpha),
        animationSpec = trial3ControlSpec(),
        label = "toggle-fill",
    )

    Box(
        modifier = modifier
            .width(56.dp)
            .height(32.dp)
            .border(Stroke.hair, ink.copy(alpha = Alpha.border * alpha))
            .semantics { if (label != null) contentDescription = label }
            // toggleable rather than clickable: it is what tells the platform this
            // is a switch and what state it is in, so a screen reader says "on"
            // and "off" instead of announcing an anonymous button.
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(24.dp)
                .background(knobColor),
        )
    }
}

/** A box that is checked by being filled. Square, like everything else. */
@Composable
public fun Trial3Check(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    size: Dp = 22.dp,
) {
    val ink = Trial3.colors.ink
    val alpha = if (enabled) 1f else Alpha.disabled
    val fill by animateColorAsState(
        targetValue = if (checked) ink.copy(alpha = alpha) else Color.Transparent,
        animationSpec = trial3ControlSpec(),
        label = "check-fill",
    )
    Box(
        modifier = modifier
            .size(TouchTarget)
            .semantics { if (label != null) contentDescription = label }
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .border(Stroke.hair, ink.copy(alpha = Alpha.border * alpha))
                .padding(4.dp)
                .background(fill),
        )
    }
}

/** Square chip. Selected means filled, not tinted and not outlined-in-accent. */
@Composable
public fun Trial3Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = Trial3.colors
    val alpha = if (enabled) 1f else Alpha.disabled
    val fillColor by animateColorAsState(
        targetValue = if (selected) colors.ink.copy(alpha = alpha) else Color.Transparent,
        animationSpec = trial3ControlSpec(),
        label = "chip-fill",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.ink.copy(alpha = alpha) else colors.line.copy(alpha = alpha),
        animationSpec = trial3ControlSpec(),
        label = "chip-border",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) colors.background else colors.ink.copy(alpha = alpha),
        animationSpec = trial3ControlSpec(),
        label = "chip-label",
    )

    Box(
        modifier = modifier
            .height(SmallControlHeight)
            .background(fillColor)
            .border(Stroke.hair, borderColor)
            .semantics { role = Role.Button }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Space.md),
        contentAlignment = Alignment.Center,
    ) {
        Trial3Text(
            text = label,
            style = Trial3.typography.labelMedium,
            color = labelColor,
            maxLines = 1,
        )
    }
}

/**
 * One of a few choices, laid out as a row of touching cells.
 *
 * A row of chips says "any number of these"; a segmented row says "exactly one
 * of these", and the difference is worth drawing. The cells share their borders,
 * so the group reads as one control rather than as three buttons that happen to
 * be adjacent.
 */
@Composable
public fun Trial3Segmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = Trial3.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SmallControlHeight)
            .border(Stroke.hair, colors.line),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(Stroke.hair)
                        .height(SmallControlHeight)
                        .background(colors.line),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(SmallControlHeight)
                    .background(if (selected) colors.ink else Color.Transparent)
                    .semantics { role = Role.RadioButton }
                    .clickable(enabled = enabled) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Trial3Text(
                    text = option,
                    style = Trial3.typography.labelMedium,
                    color = if (selected) colors.background else colors.ink,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * A value on a line, with a square knob.
 *
 * Steps are the default rather than the exception: a continuous slider promises
 * a precision a thumb on a phone does not have, and every setting this library
 * was written for -- a target, an hour, a number of cards -- is a whole number.
 * Pass steps = 0 for genuinely continuous values.
 */
@Composable
public fun Trial3Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    label: String? = null,
) {
    val colors = Trial3.colors
    val alpha = if (enabled) 1f else Alpha.disabled
    val safe = value.coerceIn(0f, 1f)

    fun quantize(raw: Float): Float {
        val clamped = raw.coerceIn(0f, 1f)
        if (steps <= 0) return clamped
        val stepCount = steps + 1
        return (clamped * stepCount).roundToInt().toFloat() / stepCount
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TouchTarget)
            .semantics { if (label != null) contentDescription = label }
            .pointerInput(enabled, steps) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    onValueChange(quantize(offset.x / size.width.toFloat()))
                }
            }
            .pointerInput(enabled, steps) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        onValueChange(quantize(offset.x / size.width.toFloat()))
                    },
                    onHorizontalDrag = { change, _ ->
                        onValueChange(quantize(change.position.x / size.width.toFloat()))
                    },
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        // The line, and the part of it that is behind the knob.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Stroke.hair)
                .background(colors.line.copy(alpha = alpha)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(safe)
                .height(Stroke.heavy)
                .background(colors.accent.copy(alpha = alpha)),
        )
        // The knob. Offset by the fraction of the track, using a layout rather
        // than a pixel calculation so it stays correct at every screen width.
        Row(modifier = Modifier.fillMaxWidth()) {
            if (safe > 0f) Box(modifier = Modifier.weight(safe))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(colors.ink.copy(alpha = alpha)),
            )
            if (safe < 1f) Box(modifier = Modifier.weight(1f - safe))
        }
    }
}

/**
 * A number with a minus and a plus, for a value somebody adjusts by one.
 *
 * This is what replaces a slider for small integers. A slider that spans four
 * values is a control where every value is a near miss.
 */
@Composable
public fun Trial3Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..99,
    step: Int = 1,
    enabled: Boolean = true,
    format: (Int) -> String = { it.toString() },
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        Trial3Button(
            label = "-",
            onClick = { onValueChange((value - step).coerceIn(range.first, range.last)) },
            enabled = enabled && value > range.first,
        )
        Trial3Text(
            text = format(value),
            style = Trial3.typography.titleMedium,
            color = Trial3.colors.ink.copy(alpha = if (enabled) 1f else Alpha.disabled),
            maxLines = 1,
        )
        Trial3Button(
            label = "+",
            onClick = { onValueChange((value + step).coerceIn(range.first, range.last)) },
            enabled = enabled && value < range.last,
        )
    }
}

/**
 * One choice out of several.
 *
 * A square that fills in when it is chosen, because this design has no circles.
 * The difference between chosen and not chosen is a solid block of ink inside a
 * hairline -- readable at arm's length on a dimmed screen, which a thin ring is
 * not.
 *
 * The mark is [size], but the target is a full [TouchTarget], so a list of
 * options stays comfortable to hit without the marks growing into buttons.
 */
@Composable
public fun Trial3Radio(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    size: Dp = 22.dp,
) {
    val colors = Trial3.colors
    val alpha = if (enabled) 1f else Alpha.disabled
    val isSelected = selected
    Box(
        modifier = modifier
            .size(TouchTarget)
            .semantics {
                role = Role.RadioButton
                this.selected = isSelected
                if (label != null) contentDescription = label
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .border(Stroke.hair, colors.ink.copy(alpha = alpha)),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(size - 8.dp)
                        .background(colors.ink.copy(alpha = alpha)),
                )
            }
        }
    }
}
