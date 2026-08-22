package dev.lattice.ui.graphic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lattice.ui.Lattice
import dev.lattice.ui.token.Motion
import dev.lattice.ui.token.latControlSpec

/*
 * The field: a grid of cells whose brightness stands for how much of something
 * exists.
 *
 * This is the library's substitute for a pie, a donut and a ring gauge. It is
 * countable -- a user can see that eleven of forty cells are lit -- it needs no
 * legend, and it survives being 40dp tall on a widget. It is also the one visual
 * that scales from a header illustration down to a launcher tile without changing
 * shape.
 */

/**
 * The state of one cell, as a pure function of its index and the field's density.
 *
 * Extracted and deterministic so it can be tested and so a widget can draw the
 * same field a screen just drew. The scatter comes from an integer hash rather
 * than from Random: a field that re-randomises on every recomposition shimmers,
 * and shimmer reads as an animation nobody asked for.
 */
public fun fieldStep(index: Int, seed: Int = 0): Float {
    var x = index * 374_761_393 + seed * 668_265_263
    x = (x xor (x shr 13)) * 1_274_126_177
    val bits = (x xor (x shr 16)) and 0xFFFF
    return bits / 65_535f
}

/**
 * A grid of cells, [fraction] of which are lit.
 *
 * @param fraction how much of the field is filled, 0..1.
 * @param columns cells across; rows are derived from the height.
 */
@Composable
public fun LatMemoryField(
    fraction: Float,
    modifier: Modifier = Modifier,
    columns: Int = 18,
    rows: Int = 5,
    height: Dp = 84.dp,
    color: Color = Lattice.colors.accent,
    dim: Color = Lattice.colors.muted,
    seed: Int = 0,
) {
    val target = if (fraction.isFinite()) fraction.coerceIn(0f, 1f) else 0f
    val shown by animateFloatAsState(
        targetValue = target,
        animationSpec = latControlSpec(Motion.contentChangeDurationMillis),
        label = "memory-field",
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val columnCount = columns.coerceIn(1, 64)
        val rowCount = rows.coerceIn(1, 32)
        val gap = 4.dp.toPx()
        val cellW = ((size.width - gap * (columnCount - 1)) / columnCount).coerceAtLeast(0f)
        val cellH = ((size.height - gap * (rowCount - 1)) / rowCount).coerceAtLeast(0f)
        val total = columnCount * rowCount
        val lit = (shown * total).toInt()

        for (index in 0 until total) {
            val column = index % columnCount
            val row = index / columnCount
            val jitter = fieldStep(index, seed)
            val on = index < lit
            val alpha = if (on) 0.55f + jitter * 0.45f else 0.06f + jitter * 0.06f
            drawRect(
                color = (if (on) color else dim).copy(alpha = alpha),
                topLeft = Offset(column * (cellW + gap), row * (cellH + gap)),
                size = Size(cellW, cellH),
            )
        }
    }
}
