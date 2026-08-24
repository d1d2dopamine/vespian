package dev.trial3lib.ui

import dev.trial3lib.ui.graphic.fieldStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The field's scatter must be stable.
 *
 * If cell brightness came from a random source, the whole field would reshuffle
 * on every recomposition and the header would shimmer. Same index, same value --
 * forever, and on any device.
 */
class FieldStepTest {

    @Test
    fun `the same cell always gets the same value`() {
        repeat(64) { index ->
            assertEquals(fieldStep(index), fieldStep(index), 0f)
        }
    }

    @Test
    fun `values stay inside the unit range`() {
        repeat(512) { index ->
            val value = fieldStep(index, seed = 7)
            assertTrue("$value out of range at $index", value in 0f..1f)
        }
    }

    @Test
    fun `neighbouring cells do not all get the same value`() {
        val distinct = (0 until 40).map { fieldStep(it) }.distinct().size
        assertTrue("only $distinct distinct values in 40 cells", distinct > 30)
    }
}
