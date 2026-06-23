package com.mrmustard.activelistening.domain.usecase

import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.guidance.GuidedListeningMarkerSuggestion
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidedSessionUseCaseTest {

    private val useCase = GuidedSessionUseCase()

    @Test
    fun `creates sections and request when song is available`() {
        val plan = useCase(
            playbackState = PlaybackState(
                positionMillis = 30_000L,
                durationMillis = 120_000L,
            ),
            songTitle = "Practice Song",
            importedSongDurationMillis = 120_000L,
        )

        assertEquals(4, plan.sections.size)
        assertEquals(plan.activeSectionId, plan.selectedSectionId)
        assertNotNull(plan.guidanceRequest)
        assertEquals(plan.sections.size, plan.guidanceRequest?.markers?.size)
    }

    @Test
    fun `omits guidance request when no song is available`() {
        val plan = useCase(
            playbackState = PlaybackState(durationMillis = 90_000L),
            songTitle = null,
            importedSongDurationMillis = null,
        )

        assertTrue(plan.sections.isNotEmpty())
        assertNull(plan.guidanceRequest)
    }

    @Test
    fun `merges ai suggestions by id and maps labels`() {
        val sections = useCase(
            playbackState = PlaybackState(durationMillis = 120_000L),
            songTitle = null,
            importedSongDurationMillis = null,
        ).sections

        val merged = useCase.mergeSuggestions(
            sections = sections,
            result = GuidedListeningResult.Success(
                markers = listOf(
                    GuidedListeningMarkerSuggestion(
                        id = sections[1].id,
                        title = "Bridge",
                        prompt = "Escucha el contraste antes del regreso.",
                    ),
                ),
            ),
        )

        assertEquals(SectionLabel.Bridge, merged[1].label)
        assertEquals("Escucha el contraste antes del regreso.", merged[1].prompt)
        assertEquals(sections[0], merged[0])
    }

    @Test
    fun `merges optional rhythm contrast by id`() {
        val sections = useCase(
            playbackState = PlaybackState(durationMillis = 120_000L),
            songTitle = null,
            importedSongDurationMillis = null,
        ).sections

        val merged = useCase.mergeSuggestions(
            sections = sections,
            result = GuidedListeningResult.Success(
                markers = listOf(
                    GuidedListeningMarkerSuggestion(
                        id = sections[2].id,
                        title = "Coro",
                        prompt = "Compara si cambia el feel.",
                        musicalContrast = SectionMusicalContrast(
                            confidence = SectionRhythmConfidence.Low,
                            explanation = "Cambio orientativo de sensacion ritmica, distinto de energia o instrumentacion.",
                        ),
                    ),
                ),
            ),
        )

        assertEquals(
            "Cambio orientativo de sensacion ritmica, distinto de energia o instrumentacion.",
            merged[2].musicalContrast?.explanation,
        )
        assertNull(merged[1].musicalContrast)
    }
}
