package dev.vespian.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Regression tests for particle selection and posterior summaries, not sleep efficacy. */
class FilterTest {
    private fun particle(weight: Double, marker: Double = 0.2, tau: Double = 24.2) =
        Particle(tau, 20.0, 18.2, 4.2, 25.0, 0.5, weight, sWake = marker)

    @Test fun `resampling preserves the sole supported particle at every position`() {
        for (winner in 0..2) {
            val f = Filter(MutableList(3) { i -> particle(if (i == winner) 1.0 else 0.0, 0.2 + i) })
            f.resample(Random(47))
            assertTrue("winner $winner", f.particles.all { it.sWake == 0.2 + winner })
            assertTrue(f.particles.all { it.weight == 1.0 / 3.0 })
        }
    }

    @Test fun `uniform resampling retains every parent once`() {
        val f = Filter(MutableList(8) { particle(1.0 / 8, it.toDouble()) })
        f.resample(Random(97))
        assertEquals((0..7).map { it.toDouble() }, f.particles.map { it.sWake })
    }

    @Test fun `zero offset skips zero mass and uses half open cdf intervals`() {
        val random = object : Random() {
            private val delegate = Random(23)
            private var first = true
            override fun nextBits(bitCount: Int): Int = delegate.nextBits(bitCount)
            override fun nextDouble(): Double {
                if (first) { first = false; return 0.0 }
                return delegate.nextDouble()
            }
        }
        val f = Filter(mutableListOf(particle(0.0, 0.0), particle(0.5, 1.0), particle(0.5, 2.0), particle(0.0, 3.0)))
        f.resample(random)
        assertEquals(listOf(1.0, 1.0, 2.0, 2.0), f.particles.map { it.sWake })
    }

    @Test fun `resampling supports unnormalised positive weights`() {
        val f = Filter(mutableListOf(particle(0.0, 0.0), particle(8.0, 1.0), particle(0.0, 2.0)))
        f.resample(Random(11))
        assertTrue(f.particles.all { it.sWake == 1.0 })
        assertEquals(1.0, f.particles.sumOf { it.weight }, 1e-12)
    }

    @Test fun `unequal resampling allocates offspring in proportion to mass`() {
        val f = Filter(MutableList(8) { i -> particle(when (i) { 0 -> 1.0; 1 -> 7.0; else -> 0.0 }, i.toDouble()) })
        f.resample(Random(17))
        assertEquals(listOf(0.0) + List(7) { 1.0 }, f.particles.map { it.sWake })
    }

    @Test fun `subnormal weights do not underflow the sampling step`() {
        val f = Filter(MutableList(6) { i -> particle(if (i < 2) Double.MIN_VALUE else 0.0, i.toDouble()) })
        f.resample(Random(19))
        assertEquals(List(3) { 0.0 } + List(3) { 1.0 }, f.particles.map { it.sWake })
    }

    @Test fun `band honours weights without a resampling event`() {
        val f = Filter(mutableListOf(particle(0.96), particle(0.02), particle(0.02)))
        assertEquals(Band(10.0, 10.0, 10.0), f.band(doubleArrayOf(10.0, 20.0, 30.0)))
    }

    @Test fun `unequal weights above the resampling threshold still shape the forecast`() {
        val f = Filter(MutableList(2000) { i -> particle(if (i < 800) 0.9 / 800 else 0.1 / 1200) })
        assertTrue(f.ess() > Filter.RESAMPLE_BELOW)
        val band = f.band(DoubleArray(2000) { if (it < 800) 10.0 else 20.0 })
        assertEquals(10.0, band.median, 0.0)
    }

    @Test fun `observe then forecast retains unequal posterior weights above the ess threshold`() {
        val f = Filter(MutableList(2000) { i ->
            particle(if (i < 800) 0.9 / 800 else 0.1 / 1200, tau = if (i < 800) 24.1 else 24.2)
        })
        f.observe(24.0, 32.0, 8.0, 0.0, sigmaScale = 100.0)
        assertTrue(f.ess() > Filter.RESAMPLE_BELOW)
        assertTrue(f.particles[0].weight > 1.0 / 2000)
        assertEquals(0.1, f.forecast(32.0, 0.0, 10, null).driftPerDay, 1e-12)
    }

    @Test fun `sorting values retains their weights`() {
        val f = Filter(mutableListOf(particle(0.05), particle(0.9), particle(0.05)))
        assertEquals(Band(10.0, 10.0, 10.0), f.band(doubleArrayOf(30.0, 10.0, 20.0)))
    }

    @Test fun `zero weight outliers never determine a band`() {
        val f = Filter(mutableListOf(particle(0.0), particle(1.0), particle(0.0)))
        assertEquals(Band(10.0, 10.0, 10.0), f.band(doubleArrayOf(-100.0, 10.0, 100.0)))
    }

    @Test fun `quantiles use the inverse empirical cdf including exact boundaries`() {
        val f = Filter(mutableListOf(particle(1.0), particle(4.0), particle(5.0)))
        assertEquals(Band(20.0, 10.0, 30.0), f.band(doubleArrayOf(10.0, 20.0, 30.0)))
    }

    @Test fun `multiplying all weights leaves a band unchanged`() {
        val f = Filter(mutableListOf(particle(0.1), particle(0.7), particle(0.2)))
        val values = doubleArrayOf(4.0, 8.0, 12.0)
        val before = f.band(values)
        f.particles.forEach { it.weight *= 100.0 }
        assertEquals(before, f.band(values))
    }

    @Test fun `forecast drift honours posterior weights`() {
        val f = Filter(mutableListOf(particle(0.98, tau = 24.1), particle(0.01, tau = 24.8), particle(0.01, tau = 25.2)))
        assertEquals(0.1, f.forecast(8.0, 0.0, 10, null).driftPerDay, 1e-12)
    }

    @Test fun `forecast bands match a single supported particle`() {
        val supported = particle(1.0, tau = 24.1)
        val one = Filter(mutableListOf(supported.copy())).forecast(8.0, 0.0, 10, 33.0)
        val many = Filter(mutableListOf(supported.copy(), particle(0.0, tau = 25.4), particle(0.0, tau = 25.0)))
            .forecast(8.0, 0.0, 10, 33.0)
        assertEquals(one.gate, many.gate)
        assertEquals(one.onset, many.onset)
        assertEquals(one.wake, many.wake)
        assertEquals(one.reverseAlarm, many.reverseAlarm)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `band rejects no particles`() { Filter(mutableListOf()).band(doubleArrayOf()) }

    @Test(expected = IllegalArgumentException::class)
    fun `band rejects mismatched values`() { Filter(mutableListOf(particle(1.0))).band(doubleArrayOf(1.0, 2.0)) }

    @Test(expected = IllegalArgumentException::class)
    fun `band rejects nonfinite values`() { Filter(mutableListOf(particle(1.0))).band(doubleArrayOf(Double.NaN)) }

    @Test(expected = IllegalArgumentException::class)
    fun `band rejects invalid weights`() { Filter(mutableListOf(particle(-1.0))).band(doubleArrayOf(1.0)) }

    @Test(expected = IllegalArgumentException::class)
    fun `resampling rejects an empty population`() { Filter(mutableListOf()).resample(Random(4)) }

    @Test(expected = IllegalArgumentException::class)
    fun `resampling rejects zero total weight`() { Filter(mutableListOf(particle(0.0))).resample(Random(4)) }

    @Test(expected = IllegalArgumentException::class)
    fun `resampling rejects nonfinite weights`() { Filter(mutableListOf(particle(Double.NaN))).resample(Random(4)) }
}
