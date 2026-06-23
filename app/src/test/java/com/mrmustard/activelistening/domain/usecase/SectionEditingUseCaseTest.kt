package com.mrmustard.activelistening.domain.usecase

import com.mrmustard.activelistening.domain.structure.SectionBoundary
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionEditingUseCaseTest {

    private val useCase = SectionEditingUseCase()

    @Test
    fun `changing label returns structure without editor state`() {
        val sections = SongStructureFactory.createInitialSections(120_000L)

        val result = useCase.changeLabel(sections, sectionId = 1, label = SectionLabel.Bridge)

        assertEquals(SectionLabel.Bridge, result.sections[1].label)
        assertEquals(1, result.selectedSectionId)
    }

    @Test
    fun `cycling status advances suggested confirmed uncertain and suggested`() {
        val sections = SongStructureFactory.createInitialSections(120_000L)

        val confirmed = useCase.cycleStatus(sections, 1)
        val uncertain = useCase.cycleStatus(confirmed.sections, 1)
        val suggested = useCase.cycleStatus(uncertain.sections, 1)

        assertEquals(SectionStatus.Confirmed, confirmed.sections[1].status)
        assertEquals(SectionStatus.Uncertain, uncertain.sections[1].status)
        assertEquals(SectionStatus.Suggested, suggested.sections[1].status)
    }

    @Test
    fun `toggling musical contrast adds and removes marker`() {
        val sections = SongStructureFactory.createInitialSections(120_000L)

        val added = useCase.toggleMusicalContrast(sections, 1)
        val removed = useCase.toggleMusicalContrast(added.sections, 1)

        assertEquals(SectionRhythmConfidence.Low, added.sections[1].musicalContrast?.confidence)
        assertEquals(null, removed.sections[1].musicalContrast)
    }

    @Test
    fun `setting boundary keeps neighboring sections aligned`() {
        val sections = SongStructureFactory.createInitialSections(120_000L)

        val result = useCase.setBoundary(
            sections = sections,
            sectionId = 1,
            boundary = SectionBoundary.End,
            positionMillis = 70_000L,
        )

        assertEquals(70_000L, result.sections[1].endMillis)
        assertEquals(70_000L, result.sections[2].startMillis)
    }

    @Test
    fun `splitting selects new right section`() {
        val sections = SongStructureFactory.createInitialSections(120_000L)

        val result = useCase.splitAtPosition(sections, 45_000L)

        assertEquals(sections.size + 1, result.sections.size)
        assertEquals(4, result.selectedSectionId)
        assertEquals(45_000L, result.sections[2].startMillis)
    }

    @Test
    fun `removing boundary keeps merged section selected`() {
        val sections = SongStructureFactory.createInitialSections(120_000L)

        val result = useCase.removeBoundaryAfter(sections, 1)

        assertEquals(sections.size - 1, result.sections.size)
        assertEquals(1, result.selectedSectionId)
        assertTrue(result.sections.zipWithNext().all { (left, right) -> left.endMillis == right.startMillis })
    }
}
