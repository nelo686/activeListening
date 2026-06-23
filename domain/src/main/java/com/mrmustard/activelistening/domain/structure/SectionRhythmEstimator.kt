package com.mrmustard.activelistening.domain.structure

import kotlin.math.abs
import kotlin.math.roundToInt

object SectionRhythmEstimator {

    fun estimate(durationMillis: Long): SectionRhythmInfo? {
        if (durationMillis < MIN_ANALYZABLE_DURATION_MILLIS) return null

        val estimatedBars = (durationMillis.toFloat() / ASSUMED_BAR_DURATION_MILLIS.toFloat())
            .roundToInt()
            .coerceAtLeast(1)
        val expectedDurationMillis = estimatedBars * ASSUMED_BAR_DURATION_MILLIS
        val driftMillis = abs(durationMillis - expectedDurationMillis)

        return when {
            driftMillis <= MODERATE_CONFIDENCE_DRIFT_MILLIS && estimatedBars % PHRASE_BAR_COUNT == 0 ->
                SectionRhythmInfo(
                    estimatedBars = estimatedBars,
                    confidence = SectionRhythmConfidence.Moderate,
                    regularity = SectionRhythmRegularity.Regular,
                )

            driftMillis <= LOW_CONFIDENCE_DRIFT_MILLIS ->
                SectionRhythmInfo(
                    estimatedBars = estimatedBars,
                    confidence = SectionRhythmConfidence.Low,
                    regularity = SectionRhythmRegularity.Approximate,
                )

            else ->
                SectionRhythmInfo(
                    estimatedBars = null,
                    confidence = SectionRhythmConfidence.Low,
                    regularity = SectionRhythmRegularity.Irregular,
                )
        }
    }

    private const val ASSUMED_BAR_DURATION_MILLIS = 2_000L
    private const val MIN_ANALYZABLE_DURATION_MILLIS = 8_000L
    private const val MODERATE_CONFIDENCE_DRIFT_MILLIS = 250L
    private const val LOW_CONFIDENCE_DRIFT_MILLIS = 700L
    private const val PHRASE_BAR_COUNT = 4
}
