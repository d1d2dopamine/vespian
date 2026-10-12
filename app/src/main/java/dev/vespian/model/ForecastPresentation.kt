package dev.vespian.model

/** Presentation floor only. One recorded night permits the existing output;
 * it does not establish forecast accuracy, calibration, or clinical validity.
 * Keep Engine's existing count definition and model calculations unchanged.
 */
object ForecastPresentation {
    fun canPresent(nights: Int): Boolean = nights > 0
}
