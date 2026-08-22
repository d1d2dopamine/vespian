package dev.lattice.ui

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The library must not be able to see Material at all.
 *
 * A design system that merely avoids Material in its own code is one accidental
 * import away from pulling the whole artifact back in -- and with it the rounded
 * geometry, the ripple and the tonal surfaces this library exists to remove.
 * Material 3 is not a dependency of this module, so its classes must not be
 * loadable, and that is a fact a unit test can assert cheaply.
 */
class NoMaterialDependencyTest {

    private fun onClasspath(className: String): Boolean = try {
        Class.forName(className, false, this::class.java.classLoader)
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    @Test
    fun `material3 is not on the classpath`() {
        for (name in listOf(
            "androidx.compose.material3.MaterialTheme",
            "androidx.compose.material3.TextKt",
            "androidx.compose.material.MaterialTheme",
        )) {
            assertTrue("$name should not be reachable from the design library", !onClasspath(name))
        }
    }

    @Test
    fun `the foundation layer this library is built on is present`() {
        assertTrue(onClasspath("androidx.compose.ui.graphics.Color"))
    }
}
