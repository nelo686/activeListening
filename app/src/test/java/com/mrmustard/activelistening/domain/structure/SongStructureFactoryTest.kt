package com.mrmustard.activelistening.domain.structure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SongStructureFactoryTest {

    @Test
    fun `returns no sections when duration is unknown`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 0L)

        assertTrue(sections.isEmpty())
    }

    @Test
    fun `creates ordered sections without gaps or overlaps`() {
        val durationMillis = 240_000L

        val sections = SongStructureFactory.createInitialSections(durationMillis)

        assertEquals(4, sections.size)
        assertEquals(0L, sections.first().startMillis)
        assertEquals(durationMillis, sections.last().endMillis)
        assertTrue(sections.zipWithNext().all { (left, right) ->
            left.endMillis == right.startMillis && left.startMillis < left.endMillis
        })
    }

    @Test
    fun `creates one section for short songs`() {
        val durationMillis = 8_000L

        val sections = SongStructureFactory.createInitialSections(durationMillis)

        assertEquals(1, sections.size)
        assertEquals(0L, sections.single().startMillis)
        assertEquals(durationMillis, sections.single().endMillis)
    }

    @Test
    fun `finds active section from playback position`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        assertEquals(0, SongStructureFactory.activeSectionId(sections, 0L))
        assertEquals(1, SongStructureFactory.activeSectionId(sections, 30_000L))
        assertEquals(3, SongStructureFactory.activeSectionId(sections, 119_999L))
        assertEquals(3, SongStructureFactory.activeSectionId(sections, 120_000L))
        assertNull(SongStructureFactory.activeSectionId(emptyList(), 10_000L))
    }

    @Test
    fun `adjusts section end and keeps neighboring boundaries aligned`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val adjusted = SongStructureFactory.adjustSectionBoundary(
            sections = sections,
            sectionId = 1,
            boundary = SectionBoundary.End,
            deltaMillis = 5_000L,
        )

        assertEquals(65_000L, adjusted[1].endMillis)
        assertEquals(65_000L, adjusted[2].startMillis)
        assertTrue(adjusted.zipWithNext().all { (left, right) -> left.endMillis == right.startMillis })
    }

    @Test
    fun `adjusts section start and keeps minimum duration`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val adjusted = SongStructureFactory.adjustSectionBoundary(
            sections = sections,
            sectionId = 1,
            boundary = SectionBoundary.Start,
            deltaMillis = -60_000L,
        )

        assertEquals(SongStructureFactory.MIN_SECTION_DURATION_MILLIS, adjusted[0].endMillis)
        assertEquals(SongStructureFactory.MIN_SECTION_DURATION_MILLIS, adjusted[1].startMillis)
        assertTrue(adjusted.all { it.durationMillis >= SongStructureFactory.MIN_SECTION_DURATION_MILLIS })
    }
}
