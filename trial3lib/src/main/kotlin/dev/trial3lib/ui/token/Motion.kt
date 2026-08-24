package dev.trial3lib.ui.token

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max

/*
 * How the interface moves.
 *
 * Two rules. Anything the finger is holding follows a spring, because a spring
 * is what a hand expects from an object with weight. Anything leaving the screen
 * accelerates away and does not bounce, because it is gone and a bounce would
 * invite the eye to follow it.
 *
 * Nothing here loops, pulses or breathes. Motion is feedback, and feedback that
 * repeats itself is decoration. The single exception is the indeterminate
 * progress mark, which has nothing else to say -- and it is switched off with
 * everything else when motion is disabled.
 */

/**
 * Whether restrained interface feedback may animate.
 *
 * Provided once by [dev.trial3lib.ui.Trial3Theme] so no primitive invents its own
 * accessibility policy, and so an in-flight transition snaps to its destination
 * the moment motion is turned off.
 */
public val LocalTrial3MotionEnabled = staticCompositionLocalOf { true }

public object Motion {
    /** A complete Shared Axis X route change. */
    public const val sharedAxisDurationMillis: Int = 280

    /** Incoming content waits until the outgoing route is fully clear. */
    public const val sharedAxisFadeInDelayMillis: Int = 90

    /** The new route reaches full opacity exactly as its translation finishes. */
    public const val sharedAxisFadeInDurationMillis: Int = 190

    /** The old route owns the first phase, then yields the surface completely. */
    public const val sharedAxisFadeOutDurationMillis: Int = 90

    /** Small enough to read as continuity rather than as a full-screen carousel. */
    public val sharedAxisTravel = 14.dp

    /** Section jumps use a calm rhythm, not navigation geometry. */
    public const val sectionScrollDurationMillis: Int = 180

    /** Switches, chips and enabled states acknowledge a deliberate tap. */
    public const val controlChangeDurationMillis: Int = 160

    /** Conditional content changes shape without jumping. */
    public const val contentChangeDurationMillis: Int = 200

    /** Progress follows real work while filtering noisy updates. */
    public const val progressChangeDurationMillis: Int = 260

    /** One full pass of the indeterminate mark. */
    public const val busyCycleMillis: Int = 1200

    /** Back to rest, under the finger. Slightly underdamped: it has weight. */
    public val settle: AnimationSpec<Float> =
        spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 0.5f)

    /** Something taking its place. Barely visible, and that is the point. */
    public val arrive: AnimationSpec<Float> =
        spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessMedium, visibilityThreshold = 0.001f)

    /** A value that just changed and wants to be noticed once. */
    public val reveal: AnimationSpec<Float> =
        spring(dampingRatio = 0.70f, stiffness = Spring.StiffnessLow, visibilityThreshold = 0.001f)

    /**
     * Something that has been thrown.
     *
     * The duration comes from how hard it was thrown: a flick leaves fast, a slow
     * deliberate push takes its time, so the screen agrees with the hand instead
     * of playing a fixed animation over it. [haste] is what the gesture weighs:
     * above 1 it is quicker than the throw, below 1 it drags.
     */
    public fun thrown(speedPxPerSecond: Float, haste: Float = 1f): AnimationSpec<Float> {
        val speed = max(abs(speedPxPerSecond), 0f)
        val base = (220f - speed / 45f).coerceIn(120f, 220f)
        val scale = if (haste <= 0f) 1f else haste
        val millis = (base / scale).coerceIn(90f, 340f).toInt()
        return tween(durationMillis = millis, easing = FastOutLinearInEasing)
    }
}

/**
 * The spec every control in this library animates with, or an instant snap when
 * the app's motion setting is off.
 *
 * One function instead of the same six-line `if (motionEnabled) tween(...) else
 * snap()` repeated in every component, which is exactly where a control ends up
 * animating after the user asked it not to.
 */
@Composable
public fun <T> trial3ControlSpec(
    durationMillis: Int = Motion.controlChangeDurationMillis,
): FiniteAnimationSpec<T> = if (LocalTrial3MotionEnabled.current) {
    tween(durationMillis = durationMillis, easing = LinearOutSlowInEasing)
} else {
    snap()
}
