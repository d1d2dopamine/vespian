package dev.lattice.ui.token

import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/*
 * The grid.
 *
 * Nobody sees a single spacing value. Everybody sees the regularity, or its
 * absence: an interface off the grid reads as homemade even when no individual
 * screen is wrong. Everything below is a multiple of four -- small enough that no
 * layout has to fight it, large enough that the steps stay distinguishable -- and
 * the scale is short on purpose, so choosing is instant and a screen cannot
 * drift.
 */
public object Space {
    /** Rules and borders. The only value off the grid, because it is a line. */
    public val hair = 1.dp

    /** Between a label and the thing it labels. */
    public val xs = 4.dp

    /** Inside a row. */
    public val sm = 8.dp

    /** Between related rows. */
    public val md = 12.dp

    /** Screen margin, and between unrelated rows. */
    public val lg = 20.dp

    /** Between sections. */
    public val xl = 32.dp

    /** Around something that is alone on the screen. */
    public val xxl = 48.dp
}

/** The screen margin. One value, every screen, so the left edge never jumps. */
public val Edge = Space.lg

/** Nothing pressable is smaller than this. */
public val TouchTarget = 44.dp

/** Top and bottom bars. */
public val BarHeight = 56.dp

/** The height of a control you press: buttons, fields, rows. */
public val ControlHeight = 56.dp

/** The height of a small control: chips, compact fields. */
public val SmallControlHeight = 40.dp

/**
 * Reading width.
 *
 * Text that runs the full width of a tablet is text nobody finishes: the eye
 * loses the start of the next line. Phones never reach this, so it costs nothing
 * where it does not apply.
 */
public val ReadableWidth = 560.dp

public fun Modifier.readable(): Modifier = this.widthIn(max = ReadableWidth)

/**
 * Every corner is a right angle. This is the whole shape system, and it is a
 * rectangle rather than a zero-radius rounded rectangle so that no clip path is
 * ever computed for it.
 *
 * This is also the reason this library exists at all: a Material 3 component does
 * not read a theme shape scheme. It reads its own token, and for buttons,
 * switches and chips that token is CornerFull, hardcoded to a circle. A square
 * shape scheme therefore does nothing to them, and one stock button left on a
 * screen is enough to make an app look like two apps stitched together.
 */
public object LatShape {
    public val square: Shape = RectangleShape
}

/** Border weights. A control you press and a note you read are not the same weight. */
public object Stroke {
    /** Buttons, fields, rules, cell borders. */
    public val hair = 1.dp

    /** A block of text the app is telling you. */
    public val heavy = 2.dp
}

/** Standard opacities, so "disabled" means one thing across the whole interface. */
public object Alpha {
    public const val disabled: Float = 0.35f
    public const val quiet: Float = 0.60f
    public const val trackOff: Float = 0.22f
    public const val border: Float = 0.55f
    public const val panelBorder: Float = 0.45f
}
