package com.mrmustard.activelistening.domain.structure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SongStructureEditorTest {

    @Test
    fun `changes only selected section label`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val updated = SongStructureEditor.changeLabel(
            sections = sections,
            sectionId = 1,
            label = SectionLabel.Bridge,
        )

        assertEquals(SectionLabel.Bridge, updated[1].label)
        assertEquals(sections[0], updated[0])
        assertEquals(sections[2], updated[2])
    }

    @Test
    fun `confirming section clears approximate state`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val updated = SongStructureEditor.changeStatus(
            sections = sections,
            sectionId = 1,
            status = SectionStatus.Confirmed,
        )

        assertEquals(SectionStatus.Confirmed, updated[1].status)
        assertFalse(updated[1].isApproximate)
    }

    @Test
    fun `marking section uncertain keeps other sections unchanged`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val updated = SongStructureEditor.changeStatus(
            sections = sections,
            sectionId = 2,
            status = SectionStatus.Uncertain,
        )

        assertEquals(SectionStatus.Uncertain, updated[2].status)
        assertEquals(sections[0], updated[0])
        assertEquals(sections[1], updated[1])
    }

    @Test
    fun `custom label uses other category and trims its name`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val updated = SongStructureEditor.changeCustomLabel(sections, 1, "  Pre-coro  ")

        assertEquals(SectionLabel.Other, updated[1].label)
        assertEquals("Pre-coro", updated[1].customLabel)
    }
}
