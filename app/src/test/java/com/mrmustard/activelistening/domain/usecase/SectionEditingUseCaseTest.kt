package com.mrmustard.activelistening.domain.usecase

import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.structure.SectionBoundary
import com.mrmustard.activelistening.domain.structure.SectionLabel
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
        assertTrue(result.learningContent?.summary.orEmpty().isNotBlank())
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
}
