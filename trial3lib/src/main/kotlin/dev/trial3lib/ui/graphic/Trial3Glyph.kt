package dev.trial3lib.ui.graphic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.trial3lib.ui.Trial3

/*
 * The marks.
 *
 * Forty-nine of them, drawn from straight lines, rectangles and -- twice -- a
 * quadratic. No icon font and no vector assets, for three reasons that all
 * matter more than the convenience of a ready-made set:
 *
 *   1. An icon font brings rounded geometry back in through the side door. Every
 *      stock set is drawn on a 2dp rounded stroke, and next to a square button
 *      it reads as an icon borrowed from another app.
 *   2. A mark drawn here takes the palette's colour and the current alpha for
 *      free, at any size, with no tint list and no asset per density.
 *   3. The same code draws into a widget's canvas or a chart's, through
 *      [drawGlyph], so the mark on the home screen is the mark in the app.
 *
 * Every glyph fits a square viewport of 24 units and is scaled from the smaller
 * dimension, so a 16dp mark and a 48dp mark carry the same weight relative to
 * their box. Stroke width is derived from the box as well: a hairline that stays
 * one pixel while the mark grows looks like a printing error.
 */
public enum class Trial3Glyph {
    PLUS,
    MINUS,
    CLOSE,
    CHECK,
    ARROW_LEFT,
    ARROW_RIGHT,
    ARROW_UP,
    ARROW_DOWN,
    CHEVRON_LEFT,
    CHEVRON_RIGHT,
    CHEVRON_UP,
    CHEVRON_DOWN,
    MENU,
    SEARCH,
    SETTINGS,
    EDIT,
    TRASH,
    DOWNLOAD,
    UPLOAD,
    SHARE,
    COPY,
    FOLDER,
    FILE,
    DECK,
    LAYERS,
    SYNC,
    CHART,
    CLOCK,
    ALARM,
    MOON,
    SUN,
    BED,
    HEART,
    PULSE,
    CUP,
    FLASK,
    BATTERY,
    BLUETOOTH,
    BELL,
    WARNING,
    SEND,
    KEY,
    PHONE,
    PLAY,
    CIRCLE,
    CHECK_CIRCLE,
    SLIDERS,
    SHIELD,
    FLAG,
}

/**
 * A mark, on its own.
 *
 * Not clickable: use [dev.trial3lib.ui.component.Trial3IconButton] for that, which
 * gives it a 44dp target. A bare glyph with a clickable on it is a 20dp target,
 * and a 20dp target at a screen edge loses every argument with the system's own
 * gestures.
 *
 * @param label what a screen reader says. A mark carries no text, so without
 *   this it is invisible to anyone not looking at it.
 */
@Composable
public fun Trial3GlyphIcon(
    glyph: Trial3Glyph,
    modifier: Modifier = Modifier,
    color: Color = Trial3.colors.ink,
    size: Dp = 20.dp,
    label: String? = null,
) {
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { if (label != null) contentDescription = label },
    ) {
        drawGlyph(glyph = glyph, color = color)
    }
}

/**
 * Draw a mark into a canvas that is already being drawn.
 *
 * For a widget, a chart legend, or a mark inside a row of cells. [origin] and
 * [side] carve a square out of the current canvas; by default the mark fills it.
 */
public fun DrawScope.drawGlyph(
    glyph: Trial3Glyph,
    color: Color,
    origin: Offset = Offset.Zero,
    side: Float = size.minDimension,
    strokeWidth: Float = side / 11f,
) {
    val u = side / 24f
    val cap = StrokeCap.Butt

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float = strokeWidth) {
        drawLine(
            color = color,
            start = Offset(origin.x + x1 * u, origin.y + y1 * u),
            end = Offset(origin.x + x2 * u, origin.y + y2 * u),
            strokeWidth = width,
            cap = cap,
        )
    }

    fun box(left: Float, top: Float, right: Float, bottom: Float, filled: Boolean = false) {
        val topLeft = Offset(origin.x + left * u, origin.y + top * u)
        val boxSize = Size((right - left) * u, (bottom - top) * u)
        if (filled) {
            drawRect(color = color, topLeft = topLeft, size = boxSize)
        } else {
            drawRect(color = color, topLeft = topLeft, size = boxSize, style = Stroke(strokeWidth))
        }
    }

    fun path(build: Path.(Float) -> Unit, filled: Boolean = false) {
        val p = Path()
        p.build(u)
        if (filled) drawPath(p, color) else drawPath(p, color, style = Stroke(strokeWidth))
    }

    fun Path.at(x: Float, y: Float, unit: Float, move: Boolean = false) {
        val px = origin.x + x * unit
        val py = origin.y + y * unit
        if (move) moveTo(px, py) else lineTo(px, py)
    }

    when (glyph) {
        Trial3Glyph.PLUS -> {
            line(4f, 12f, 20f, 12f)
            line(12f, 4f, 12f, 20f)
        }
        Trial3Glyph.MINUS -> line(4f, 12f, 20f, 12f)
        Trial3Glyph.CLOSE -> {
            line(5f, 5f, 19f, 19f)
            line(19f, 5f, 5f, 19f)
        }
        Trial3Glyph.CHECK -> {
            line(4f, 13f, 10f, 19f)
            line(10f, 19f, 20f, 6f)
        }
        Trial3Glyph.ARROW_LEFT -> {
            line(3f, 12f, 21f, 12f)
            line(3f, 12f, 10f, 5f)
            line(3f, 12f, 10f, 19f)
        }
        Trial3Glyph.ARROW_RIGHT -> {
            line(3f, 12f, 21f, 12f)
            line(21f, 12f, 14f, 5f)
            line(21f, 12f, 14f, 19f)
        }
        Trial3Glyph.ARROW_UP -> {
            line(12f, 21f, 12f, 3f)
            line(12f, 3f, 5f, 10f)
            line(12f, 3f, 19f, 10f)
        }
        Trial3Glyph.ARROW_DOWN -> {
            line(12f, 3f, 12f, 21f)
            line(12f, 21f, 5f, 14f)
            line(12f, 21f, 19f, 14f)
        }
        Trial3Glyph.CHEVRON_LEFT -> {
            line(16f, 4f, 8f, 12f)
            line(8f, 12f, 16f, 20f)
        }
        Trial3Glyph.CHEVRON_RIGHT -> {
            line(8f, 4f, 16f, 12f)
            line(16f, 12f, 8f, 20f)
        }
        Trial3Glyph.CHEVRON_UP -> {
            line(4f, 16f, 12f, 8f)
            line(12f, 8f, 20f, 16f)
        }
        Trial3Glyph.CHEVRON_DOWN -> {
            line(4f, 8f, 12f, 16f)
            line(12f, 16f, 20f, 8f)
        }
        Trial3Glyph.MENU -> {
            line(3f, 6f, 21f, 6f)
            line(3f, 12f, 21f, 12f)
            line(3f, 18f, 21f, 18f)
        }
        Trial3Glyph.SEARCH -> {
            // A square lens. A circular one would be the only curve on the screen.
            box(4f, 4f, 15f, 15f)
            line(15f, 15f, 21f, 21f)
        }
        Trial3Glyph.SETTINGS -> {
            line(3f, 7f, 21f, 7f)
            line(3f, 17f, 21f, 17f)
            box(8f, 4f, 12f, 10f, filled = true)
            box(14f, 14f, 18f, 20f, filled = true)
        }
        Trial3Glyph.EDIT -> {
            line(4f, 20f, 4f, 16f)
            line(4f, 16f, 16f, 4f)
            line(16f, 4f, 20f, 8f)
            line(20f, 8f, 8f, 20f)
            line(8f, 20f, 4f, 20f)
        }
        Trial3Glyph.TRASH -> {
            line(3f, 7f, 21f, 7f)
            box(6f, 7f, 18f, 21f)
            line(10f, 4f, 14f, 4f)
            line(10f, 11f, 10f, 17f)
            line(14f, 11f, 14f, 17f)
        }
        Trial3Glyph.DOWNLOAD -> {
            line(12f, 3f, 12f, 15f)
            line(12f, 15f, 6f, 9f)
            line(12f, 15f, 18f, 9f)
            line(4f, 20f, 20f, 20f)
        }
        Trial3Glyph.UPLOAD -> {
            line(12f, 16f, 12f, 4f)
            line(12f, 4f, 6f, 10f)
            line(12f, 4f, 18f, 10f)
            line(4f, 20f, 20f, 20f)
        }
        Trial3Glyph.SHARE -> {
            box(3f, 9f, 8f, 14f, filled = true)
            box(16f, 3f, 21f, 8f, filled = true)
            box(16f, 16f, 21f, 21f, filled = true)
            line(8f, 11f, 16f, 6f)
            line(8f, 12f, 16f, 18f)
        }
        Trial3Glyph.COPY -> {
            box(3f, 3f, 15f, 15f)
            box(9f, 9f, 21f, 21f)
        }
        Trial3Glyph.FOLDER -> {
            line(3f, 6f, 10f, 6f)
            line(10f, 6f, 12f, 9f)
            line(12f, 9f, 21f, 9f)
            box(3f, 6f, 21f, 20f)
        }
        Trial3Glyph.FILE -> {
            box(5f, 3f, 19f, 21f)
            line(8f, 8f, 16f, 8f)
            line(8f, 12f, 16f, 12f)
            line(8f, 16f, 13f, 16f)
        }
        Trial3Glyph.DECK -> {
            box(3f, 6f, 17f, 20f)
            line(7f, 3f, 21f, 3f)
            line(21f, 3f, 21f, 17f)
        }
        Trial3Glyph.LAYERS -> {
            line(3f, 8f, 12f, 3f)
            line(12f, 3f, 21f, 8f)
            line(21f, 8f, 12f, 13f)
            line(12f, 13f, 3f, 8f)
            line(3f, 14f, 12f, 19f)
            line(12f, 19f, 21f, 14f)
        }
        Trial3Glyph.SYNC -> {
            line(4f, 9f, 20f, 9f)
            line(20f, 9f, 15f, 4f)
            line(20f, 15f, 4f, 15f)
            line(4f, 15f, 9f, 20f)
        }
        Trial3Glyph.CHART -> {
            line(4f, 20f, 20f, 20f)
            box(5f, 13f, 9f, 20f, filled = true)
            box(11f, 8f, 15f, 20f, filled = true)
            box(17f, 4f, 21f, 20f, filled = true)
        }
        Trial3Glyph.CLOCK -> {
            box(4f, 4f, 20f, 20f)
            line(12f, 8f, 12f, 12f)
            line(12f, 12f, 16f, 14f)
        }
        Trial3Glyph.ALARM -> {
            box(5f, 6f, 19f, 20f)
            line(3f, 4f, 7f, 7f)
            line(21f, 4f, 17f, 7f)
            line(12f, 10f, 12f, 13f)
            line(12f, 13f, 15f, 15f)
        }
        Trial3Glyph.MOON -> {
            // The waning shape, drawn with one arc and one straight edge so it
            // reads as a moon and not as a circle with a bite in it.
            path({ unit ->
                at(16f, 3f, unit, move = true)
                quadraticTo(
                    origin.x + 6f * unit, origin.y + 8f * unit,
                    origin.x + 9f * unit, origin.y + 15f * unit,
                )
                quadraticTo(
                    origin.x + 12f * unit, origin.y + 22f * unit,
                    origin.x + 21f * unit, origin.y + 19f * unit,
                )
            })
        }
        Trial3Glyph.SUN -> {
            box(8f, 8f, 16f, 16f, filled = true)
            line(12f, 2f, 12f, 5f)
            line(12f, 19f, 12f, 22f)
            line(2f, 12f, 5f, 12f)
            line(19f, 12f, 22f, 12f)
        }
        Trial3Glyph.BED -> {
            line(3f, 8f, 3f, 20f)
            line(3f, 13f, 21f, 13f)
            line(21f, 13f, 21f, 20f)
            box(5f, 9f, 10f, 12f, filled = true)
            line(3f, 20f, 21f, 20f)
        }
        Trial3Glyph.HEART -> {
            // Two squares and a point: a heart with no curve in it at all.
            path({ unit ->
                at(12f, 20f, unit, move = true)
                at(4f, 12f, unit)
                at(4f, 7f, unit)
                at(8f, 5f, unit)
                at(12f, 9f, unit)
                at(16f, 5f, unit)
                at(20f, 7f, unit)
                at(20f, 12f, unit)
                close()
            }, filled = true)
        }
        Trial3Glyph.PULSE -> {
            line(2f, 13f, 8f, 13f)
            line(8f, 13f, 11f, 5f)
            line(11f, 5f, 14f, 20f)
            line(14f, 20f, 17f, 13f)
            line(17f, 13f, 22f, 13f)
        }
        Trial3Glyph.CUP -> {
            box(4f, 6f, 16f, 18f)
            line(16f, 9f, 20f, 9f)
            line(20f, 9f, 20f, 14f)
            line(20f, 14f, 16f, 14f)
            line(3f, 21f, 18f, 21f)
        }
        Trial3Glyph.FLASK -> {
            line(9f, 3f, 9f, 10f)
            line(15f, 3f, 15f, 10f)
            line(9f, 10f, 4f, 20f)
            line(15f, 10f, 20f, 20f)
            line(4f, 20f, 20f, 20f)
            line(7f, 3f, 17f, 3f)
        }
        Trial3Glyph.BATTERY -> {
            box(3f, 8f, 19f, 16f)
            box(19f, 11f, 21f, 13f, filled = true)
            box(5f, 10f, 13f, 14f, filled = true)
        }
        Trial3Glyph.BLUETOOTH -> {
            line(12f, 3f, 12f, 21f)
            line(12f, 3f, 18f, 8f)
            line(18f, 8f, 6f, 16f)
            line(12f, 21f, 18f, 16f)
            line(18f, 16f, 6f, 8f)
        }
        Trial3Glyph.BELL -> {
            box(7f, 5f, 17f, 16f)
            line(4f, 16f, 20f, 16f)
            line(10f, 19f, 14f, 19f)
            line(12f, 3f, 12f, 5f)
        }
        Trial3Glyph.WARNING -> {
            line(12f, 3f, 3f, 20f)
            line(3f, 20f, 21f, 20f)
            line(21f, 20f, 12f, 3f)
            line(12f, 9f, 12f, 14f)
            box(11f, 16f, 13f, 18f, filled = true)
        }
        Trial3Glyph.SEND -> {
            line(3f, 12f, 21f, 4f)
            line(21f, 4f, 13f, 21f)
            line(13f, 21f, 11f, 13f)
            line(11f, 13f, 3f, 12f)
        }
        Trial3Glyph.KEY -> {
            box(4f, 9f, 12f, 15f)
            line(12f, 12f, 21f, 12f)
            line(17f, 12f, 17f, 16f)
            line(20f, 12f, 20f, 16f)
        }
        Trial3Glyph.PHONE -> {
            box(7f, 3f, 17f, 21f)
            line(10f, 18f, 14f, 18f)
        }
        Trial3Glyph.PLAY -> {
            path({ unit ->
                at(7f, 4f, unit, move = true)
                at(20f, 12f, unit)
                at(7f, 20f, unit)
                close()
            }, filled = true)
        }
        Trial3Glyph.CIRCLE -> box(5f, 5f, 19f, 19f)
        Trial3Glyph.CHECK_CIRCLE -> {
            box(4f, 4f, 20f, 20f)
            line(7f, 12f, 11f, 16f)
            line(11f, 16f, 17f, 8f)
        }
        Trial3Glyph.SLIDERS -> {
            line(3f, 6f, 21f, 6f)
            line(3f, 12f, 21f, 12f)
            line(3f, 18f, 21f, 18f)
            box(6f, 4f, 8f, 8f, filled = true)
            box(14f, 10f, 16f, 14f, filled = true)
            box(9f, 16f, 11f, 20f, filled = true)
        }
        Trial3Glyph.SHIELD -> {
            line(4f, 4f, 20f, 4f)
            line(4f, 4f, 4f, 12f)
            line(20f, 4f, 20f, 12f)
            line(4f, 12f, 12f, 21f)
            line(20f, 12f, 12f, 21f)
        }
        Trial3Glyph.FLAG -> {
            line(5f, 3f, 5f, 21f)
            line(5f, 4f, 19f, 4f)
            line(19f, 4f, 19f, 13f)
            line(5f, 13f, 19f, 13f)
        }
    }
}
