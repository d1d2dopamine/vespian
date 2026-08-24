package dev.trial3lib.ui.component

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.trial3lib.ui.Trial3
import dev.trial3lib.ui.token.Alpha
import dev.trial3lib.ui.token.Edge
import dev.trial3lib.ui.token.Motion
import dev.trial3lib.ui.token.Space
import dev.trial3lib.ui.token.Stroke
import dev.trial3lib.ui.token.trial3ControlSpec
import dev.trial3lib.ui.token.readable
import kotlin.math.floor

/*
 * Telling the user what is going on.
 *
 * Three of the four things in this file replace a Material component that hides
 * information rather than showing it: a circular spinner that says nothing about
 * what is being waited for, a snackbar that can be missed, and a dialog that
 * rounds and elevates itself out of the design.
 */

/**
 * Below this, progress draws nothing at all.
 *
 * The bar and the percentage beside it are two statements about the same fact,
 * and when they disagree the user believes the number. One lit cell next to the
 * text "0%" is the app contradicting itself.
 */
public const val MIN_DRAWN_FRACTION: Float = 0.01f

/** How many cells are full, and how much of the next one is. */
public data class SegmentFill(val complete: Int, val partial: Float)

/**
 * The geometry of a segmented bar, as a pure function.
 *
 * Separated from the drawing so it can be tested, and because a widget and a
 * screen showing the same number must draw the same bar. Every non-finite and
 * out-of-range input is clamped rather than trusted: this is fed by a division
 * somewhere, and a division somewhere eventually produces NaN.
 */
public fun segmentFill(fraction: Float, segments: Int): SegmentFill {
    val count = segments.coerceAtLeast(1)
    val safe = when {
        fraction.isNaN() -> 0f
        else -> fraction.coerceIn(0f, 1f)
    }
    if (safe < MIN_DRAWN_FRACTION) return SegmentFill(complete = 0, partial = 0f)
    val exact = safe * count
    val complete = floor(exact).toInt().coerceIn(0, count)
    val remainder = if (complete >= count) 0f else exact - complete
    return SegmentFill(
        complete = complete,
        partial = if (remainder < MIN_DRAWN_FRACTION) 0f else remainder,
    )
}

/**
 * Determinate progress, as a row of cells.
 *
 * Cells rather than a continuous bar because a countable bar can be read at a
 * glance from across a table: eleven of twenty is a fact, 55% of a smooth line
 * is an impression.
 */
@Composable
public fun Trial3Progress(
    fraction: Float,
    modifier: Modifier = Modifier,
    segments: Int = 24,
    height: Dp = 10.dp,
    color: Color = Trial3.colors.accent,
    track: Color = Trial3.colors.line,
) {
    val target = if (fraction.isFinite()) fraction.coerceIn(0f, 1f) else 0f
    val shown by animateFloatAsState(
        targetValue = target,
        animationSpec = trial3ControlSpec(Motion.progressChangeDurationMillis),
        label = "progress",
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val count = segments.coerceIn(1, 96)
        val gap = 2.dp.toPx()
        val cell = ((size.width - gap * (count - 1)) / count).coerceAtLeast(0f)
        val fill = segmentFill(shown, count)
        for (index in 0 until count) {
            val left = index * (cell + gap)
            drawRect(
                color = track.copy(alpha = Alpha.trackOff),
                topLeft = Offset(left, 0f),
                size = Size(cell, size.height),
            )
            when {
                index < fill.complete -> drawRect(
                    color = color,
                    topLeft = Offset(left, 0f),
                    size = Size(cell, size.height),
                )
                index == fill.complete && fill.partial > 0f -> drawRect(
                    color = color.copy(alpha = 0.45f),
                    topLeft = Offset(left, 0f),
                    size = Size(cell * fill.partial, size.height),
                )
            }
        }
    }
}

/**
 * Work of unknown length.
 *
 * The library's only looping animation, and it exists for one reason: the honest
 * alternative -- a static mark -- cannot be told apart from a screen that has
 * stopped. Three cells travel; with motion switched off they stand still at half
 * brightness and the label carries the meaning instead.
 *
 * This should never be the whole screen. A screen that shows this for more than
 * a moment is usually a screen doing its work on the wrong thread.
 */
@Composable
public fun Trial3Busy(
    modifier: Modifier = Modifier,
    label: String? = null,
    cells: Int = 8,
    color: Color = Trial3.colors.accent,
) {
    val count = cells.coerceIn(3, 24)
    val phase = if (Trial3.motionEnabled) {
        val transition = rememberInfiniteTransition(label = "busy")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = count.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = Motion.busyCycleMillis),
            ),
            label = "busy-phase",
        )
        value
    } else {
        -1f
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        Canvas(
            modifier = Modifier
                .width(96.dp)
                .height(8.dp),
        ) {
            val gap = 2.dp.toPx()
            val cell = ((size.width - gap * (count - 1)) / count).coerceAtLeast(0f)
            val head = if (phase < 0f) -1 else phase.toInt()
            for (index in 0 until count) {
                val distance = if (head < 0) 0 else (index - head + count) % count
                val alpha = when {
                    head < 0 -> 0.45f
                    distance == 0 -> 1f
                    distance == 1 -> 0.55f
                    distance == 2 -> 0.28f
                    else -> 0.10f
                }
                drawRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(index * (cell + gap), 0f),
                    size = Size(cell, size.height),
                )
            }
        }
        if (label != null) {
            Trial3Text(
                text = label,
                style = Trial3.typography.labelMedium,
                color = Trial3.colors.muted,
                maxLines = 1,
            )
        }
    }
}

/**
 * The empty state of a list, and of a screen that has nothing to show yet.
 *
 * A dim field rather than the word "Empty": it fills the space a list would
 * occupy, so an empty screen still looks like the screen it will become.
 */
@Composable
public fun Trial3LatticePlaceholder(
    modifier: Modifier = Modifier,
    rows: Int = 3,
    height: Dp = 14.dp,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        val widths = listOf(1f, 0.78f, 0.55f, 0.9f, 0.64f)
        repeat(rows.coerceIn(1, 8)) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(widths[index % widths.size])
                    .height(height)
                    .background(Trial3.colors.line.copy(alpha = Alpha.trackOff)),
            )
        }
    }
}

/**
 * A question that has to be answered before anything else happens.
 *
 * Square, bordered, painted in the palette's panel colour, and it does not paint
 * a background behind the theme a second time. The destructive answer is on the
 * right and is the only coloured thing in it.
 */
@Composable
public fun Trial3Dialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    confirmLabel: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissLabel: String? = null,
    confirmColor: Color = Trial3.colors.ink,
    content: (@Composable () -> Unit)? = null,
) {
    val colors = Trial3.colors
    Trial3DialogShell(onDismiss = onDismiss, modifier = modifier) {
        Trial3Text(
            text = title,
            style = Trial3.typography.titleMedium,
            color = colors.ink,
        )
        if (body != null) {
            Trial3Text(
                text = body,
                style = Trial3.typography.bodyMedium,
                color = colors.muted,
            )
        }
        if (content != null) content()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            if (dismissLabel != null) {
                Trial3TextButton(label = dismissLabel, onClick = onDismiss)
                Box(Modifier.width(Space.sm))
            }
            if (confirmLabel != null && onConfirm != null) {
                Trial3TextButton(
                    label = confirmLabel,
                    onClick = onConfirm,
                    color = confirmColor,
                )
            }
        }
    }
}

/**
 * Something the user needs to know, in the layout rather than over it.
 *
 * This is the replacement for a snackbar. A snackbar is a message you can miss:
 * it appears where the eye is not, it leaves on a timer, and it takes its action
 * with it. A notice is still there when the user comes back from the settings
 * screen it told them to open, and the way out of the state it describes is
 * attached to it.
 */
@Composable
public fun Trial3Notice(
    text: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = Trial3.colors
    val edge = if (danger) colors.danger else colors.muted
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(Stroke.heavy, edge.copy(alpha = Alpha.border))
            .padding(Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        Trial3Text(
            text = text,
            style = Trial3.typography.bodyMedium,
            color = if (danger) colors.danger else colors.ink,
        )
        if (actionLabel != null && onAction != null) {
            Trial3TextButton(
                label = actionLabel,
                onClick = onAction,
                color = if (danger) colors.danger else colors.ink,
            )
        }
    }
}
