package com.mrmustard.activelistening.domain.usecase

import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.structure.SectionBoundary
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionEditingUseCaseTest {

    private val useCase = SectionEditingUseCase()

    @Test
    fun `opens editor for a valid section`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val selection = useCase.openEditor(sections, sectionId = 1)

        assertEquals(1, selection?.selectedSectionId)
        assertEquals(1, selection?.editingSectionId)
    }

    @Test
    fun `returns null when opening editor for missing section`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val selection = useCase.openEditor(sections, sectionId = 999)

        assertNull(selection)
    }

    @Test
    fun `changing label keeps editing context and refreshes learning content`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val result = useCase.changeLabel(
            sections = sections,
            sectionId = 1,
            label = SectionLabel.Bridge,
            learningLevel = LearningLevel.Intermediate,
        )

        assertEquals(SectionLabel.Bridge, result.sections[1].label)
        assertEquals(1, result.selectedSectionId)
        assertEquals(1, result.editingSectionId)
        assertNotNull(result.learningContent)
        assertTrue(result.learningContent?.summary.orEmpty().contains("puente"))
        assertTrue(result.learningContent?.details.orEmpty().contains("transición"))
    }

    @Test
    fun `cycling status advances suggested confirmed uncertain and back to suggested`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val confirmed = useCase.cycleStatus(
            sections = sections,
            sectionId = 1,
            learningLevel = LearningLevel.Intermediate,
        )
        val uncertain = useCase.cycleStatus(
            sections = confirmed.sections,
            sectionId = 1,
            learningLevel = LearningLevel.Intermediate,
        )
        val suggestedAgain = useCase.cycleStatus(
            sections = uncertain.sections,
            sectionId = 1,
            learningLevel = LearningLevel.Intermediate,
        )

        assertEquals(SectionStatus.Confirmed, confirmed.sections[1].status)
        assertEquals(SectionStatus.Uncertain, uncertain.sections[1].status)
        assertEquals(SectionStatus.Suggested, suggestedAgain.sections[1].status)
    }

    @Test
    fun `toggling musical contrast adds and removes manual change marker`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val withContrast = useCase.toggleMusicalContrast(
            sections = sections,
            sectionId = 1,
            learningLevel = LearningLevel.Intermediate,
        )
        val withoutContrast = useCase.toggleMusicalContrast(
            sections = withContrast.sections,
            sectionId = 1,
            learningLevel = LearningLevel.Intermediate,
        )

        assertEquals(SectionRhythmConfidence.Low, withContrast.sections[1].musicalContrast?.confidence)
        assertTrue(
            withContrast.sections[1].musicalContrast?.explanation.orEmpty()
                .contains("Cambio marcado manualmente"),
        )
        assertEquals(null, withoutContrast.sections[1].musicalContrast)
    }

    @Test
    fun `returns learning content for opened section editing context`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)
        val selection = useCase.openEditor(sections, sectionId = 2)

        val content = useCase.learningContent(
            sections = sections,
            editingSectionId = selection?.editingSectionId,
            learningLevel = LearningLevel.Introductory,
        )

        assertNotNull(content)
        assertTrue(content?.summary.orEmpty().contains("coro"))
        assertTrue(content?.summary.orEmpty().contains("memorable"))
    }

    @Test
    fun `setting boundary keeps neighboring sections aligned`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val result = useCase.setBoundary(
            sections = sections,
            sectionId = 1,
            boundary = SectionBoundary.End,
            positionMillis = 70_000L,
            learningLevel = LearningLevel.Introductory,
        )

        assertEquals(70_000L, result.sections[1].endMillis)
        assertEquals(70_000L, result.sections[2].startMillis)
        assertEquals(1, result.editingSectionId)
    }

    @Test
    fun `splitting at position selects new right section and refreshes learning content`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val result = useCase.splitAtPosition(
            sections = sections,
            positionMillis = 45_000L,
            learningLevel = LearningLevel.Introductory,
        )

        assertEquals(sections.size + 1, result.sections.size)
        assertEquals(4, result.selectedSectionId)
        assertEquals(4, result.editingSectionId)
        assertEquals(45_000L, result.sections[2].startMillis)
        assertNotNull(result.learningContent)
    }

    @Test
    fun `removing boundary after section keeps merged section selected`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val result = useCase.removeBoundaryAfter(
            sections = sections,
            sectionId = 1,
            learningLevel = LearningLevel.Intermediate,
        )

        assertEquals(sections.size - 1, result.sections.size)
        assertEquals(1, result.selectedSectionId)
        assertEquals(1, result.editingSectionId)
        assertEquals(90_000L, result.sections[1].endMillis)
        assertNotNull(result.learningContent)
    }
}
