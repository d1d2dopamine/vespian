package dev.trial3lib.ui

import dev.trial3lib.ui.component.MIN_DRAWN_FRACTION
import dev.trial3lib.ui.component.segmentFill
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Progress must never draw a sliver next to the number zero.
 *
 * The bar and the percentage beside it are two statements about the same fact,
 * and when they disagree the user believes the number. So the geometry is bound
 * to the same rounding the number uses.
 */
class SegmentFillTest {

    @Test
    fun `nothing is drawn below one percent`() {
        val fill = segmentFill(MIN_DRAWN_FRACTION / 2f, 24)
        assertEquals(0, fill.complete)
        assertEquals(0f, fill.partial, 0f)
    }

    @Test
    fun `a full bar has no partial cell`() {
        val fill = segmentFill(1f, 24)
        assertEquals(24, fill.complete)
        assertEquals(0f, fill.partial, 0f)
    }

    @Test
    fun `half of twenty four cells is twelve`() {
        val fill = segmentFill(0.5f, 24)
        assertEquals(12, fill.complete)
        assertEquals(0f, fill.partial, 0.001f)
    }

    @Test
    fun `out of range and non finite input is clamped instead of crashing`() {
        assertEquals(0, segmentFill(-3f, 10).complete)
        assertEquals(10, segmentFill(4f, 10).complete)
        assertEquals(0, segmentFill(Float.NaN, 10).complete)
        assertEquals(10, segmentFill(Float.POSITIVE_INFINITY, 10).complete)
    }

    @Test
    fun `a zero segment field does not divide by zero`() {
        assertEquals(1, segmentFill(1f, 0).complete)
    }
}
