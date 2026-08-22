package dev.lattice.ui.graphic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lattice.ui.Lattice
import dev.lattice.ui.component.LatText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment

/*
 * The spark, and the wordmark it sits in.
 *
 * The one shape in the library with a curve in it. It is drawn from a 108-unit
 * viewport rather than from fractions of the canvas so the same geometry can be
 * exported to an adaptive launcher icon and a notification mark without being
 * redrawn by eye.
 */

private const val VIEWPORT = 108f

/** The four-point spark: a small one overlapping a large one. */
@Composable
public fun LatSpark(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Lattice.colors.ink,
    companion: Boolean = true,
) {
    Canvas(modifier = modifier.size(size)) {
        val unit = this.size.minDimension / VIEWPORT

        fun spark(cx: Float, cy: Float, radius: Float, waist: Float): Path {
            val path = Path()
            path.moveTo((cx) * unit, (cy - radius) * unit)
            path.quadraticTo(
                (cx + waist) * unit, (cy - waist) * unit,
                (cx + radius) * unit, (cy) * unit,
            )
            path.quadraticTo(
                (cx + waist) * unit, (cy + waist) * unit,
                (cx) * unit, (cy + radius) * unit,
            )
            path.quadraticTo(
                (cx - waist) * unit, (cy + waist) * unit,
                (cx - radius) * unit, (cy) * unit,
            )
            path.quadraticTo(
                (cx - waist) * unit, (cy - waist) * unit,
                (cx) * unit, (cy - radius) * unit,
            )
            path.close()
            return path
        }

        drawPath(spark(cx = 46f, cy = 56f, radius = 44f, waist = 9f), color)
        if (companion) {
            drawPath(
                path = spark(cx = 88f, cy = 24f, radius = 20f, waist = 4f),
                color = color.copy(alpha = 0.82f),
            )
        }
    }
}

/**
 * The product name set in the app's own voice: the spark, then the word in the
 * display face with wide tracking.
 *
 * A wordmark is text, not an image, so it takes the palette's ink and the user's
 * chosen font like everything else, and it is legible at any size without an
 * asset per density.
 */
@Composable
public fun LatWordmark(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Lattice.colors.ink,
    markSize: Dp = 22.dp,
    spark: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (spark) LatSpark(size = markSize, color = color)
        LatText(
            text = text,
            style = Lattice.typography.headlineMedium,
            color = color,
            maxLines = 1,
        )
    }
}
