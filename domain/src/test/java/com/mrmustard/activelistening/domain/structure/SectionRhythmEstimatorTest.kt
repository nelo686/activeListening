package com.mrmustard.activelistening.domain.structure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SectionRhythmEstimatorTest {

    @Test
    fun `estimates regular bars with moderate confidence`() {
        val result = SectionRhythmEstimator.estimate(durationMillis = 16_000L)

        assertEquals(8, result?.estimatedBars)
        assertEquals(SectionRhythmConfidence.Moderate, result?.confidence)
        assertEquals(SectionRhythmRegularity.Regular, result?.regularity)
    }

    @Test
    fun `keeps low confidence for approximate divisions`() {
        val result = SectionRhythmEstimator.estimate(durationMillis = 16_500L)

        assertEquals(8, result?.estimatedBars)
        assertEquals(SectionRhythmConfidence.Low, result?.confidence)
        assertEquals(SectionRhythmRegularity.Approximate, result?.regularity)
    }

    @Test
    fun `does not force standard bars for irregular duration`() {
        val result = SectionRhythmEstimator.estimate(durationMillis = 17_250L)

        assertNull(result?.estimatedBars)
        assertEquals(SectionRhythmConfidence.Low, result?.confidence)
        assertEquals(SectionRhythmRegularity.Irregular, result?.regularity)
    }

    @Test
    fun `returns no rhythm info when duration is too short`() {
        val result = SectionRhythmEstimator.estimate(durationMillis = 6_000L)

        assertNull(result)
    }
}
