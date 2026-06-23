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
    fun `creates rhythm info for analyzable initial sections`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 128_000L)

        assertEquals(16, sections.first().rhythmInfo?.estimatedBars)
        assertEquals(SectionRhythmRegularity.Regular, sections.first().rhythmInfo?.regularity)
    }

    @Test
    fun `local prompts guide active listening decisions`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 240_000L)
        val combinedPrompts = sections.joinToString(separator = " ") { it.prompt.lowercase() }

        assertTrue(combinedPrompts.contains("energía"))
        assertTrue(combinedPrompts.contains("instrumentación"))
        assertTrue(combinedPrompts.contains("repite") || combinedPrompts.contains("repetición"))
        assertTrue(combinedPrompts.contains("ritmo") || combinedPrompts.contains("rítmico"))
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
    fun `sets section start from an absolute time and keeps neighboring boundaries aligned`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val adjusted = SongStructureFactory.setSectionBoundary(
            sections = sections,
            sectionId = 1,
            boundary = SectionBoundary.Start,
            positionMillis = 32_000L,
        )

        assertEquals(32_000L, adjusted[0].endMillis)
        assertEquals(32_000L, adjusted[1].startMillis)
        assertTrue(adjusted.zipWithNext().all { (left, right) -> left.endMillis == right.startMillis })
    }

    @Test
    fun `sets section end from an absolute time and keeps neighboring boundaries aligned`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val adjusted = SongStructureFactory.setSectionBoundary(
            sections = sections,
            sectionId = 1,
            boundary = SectionBoundary.End,
            positionMillis = 70_000L,
        )

        assertEquals(70_000L, adjusted[1].endMillis)
        assertEquals(70_000L, adjusted[2].startMillis)
        assertEquals(SectionRhythmRegularity.Regular, adjusted[1].rhythmInfo?.regularity)
        assertTrue(adjusted.zipWithNext().all { (left, right) -> left.endMillis == right.startMillis })
    }

    @Test
    fun `recalculates rhythm info when timing changes`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 128_000L)

        val adjusted = SongStructureFactory.setSectionBoundary(
            sections = sections,
            sectionId = 1,
            boundary = SectionBoundary.End,
            positionMillis = 51_250L,
        )

        assertEquals(SectionRhythmRegularity.Irregular, adjusted[1].rhythmInfo?.regularity)
        assertNull(adjusted[1].rhythmInfo?.estimatedBars)
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

    @Test
    fun `splits section at valid position and keeps timeline aligned`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val split = SongStructureFactory.splitSectionAt(
            sections = sections,
            positionMillis = 45_000L,
        )

        assertEquals(sections.size + 1, split.size)
        assertEquals(30_000L, split[1].startMillis)
        assertEquals(45_000L, split[1].endMillis)
        assertEquals(45_000L, split[2].startMillis)
        assertEquals(60_000L, split[2].endMillis)
        assertEquals(SectionLabel.Other, split[2].label)
        assertEquals(4, split[2].id)
        assertTrue(split.zipWithNext().all { (left, right) -> left.endMillis == right.startMillis })
        assertTrue(split.all { it.durationMillis >= SongStructureFactory.MIN_SECTION_DURATION_MILLIS })
    }

    @Test
    fun `does not split section when position would create too short section`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val split = SongStructureFactory.splitSectionAt(
            sections = sections,
            positionMillis = 32_000L,
        )

        assertEquals(sections, split)
    }

    @Test
    fun `removes boundary after section by merging it with next section`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val merged = SongStructureFactory.removeBoundaryAfter(
            sections = sections,
            sectionId = 1,
        )

        assertEquals(sections.size - 1, merged.size)
        assertEquals(30_000L, merged[1].startMillis)
        assertEquals(90_000L, merged[1].endMillis)
        assertEquals(sections[1].id, merged[1].id)
        assertEquals(sections[1].label, merged[1].label)
        assertTrue(merged.zipWithNext().all { (left, right) -> left.endMillis == right.startMillis })
    }

    @Test
    fun `does not remove boundary after last or missing section`() {
        val sections = SongStructureFactory.createInitialSections(durationMillis = 120_000L)

        val afterLast = SongStructureFactory.removeBoundaryAfter(
            sections = sections,
            sectionId = sections.last().id,
        )
        val afterMissing = SongStructureFactory.removeBoundaryAfter(
            sections = sections,
            sectionId = 999,
        )

        assertEquals(sections, afterLast)
        assertEquals(sections, afterMissing)
    }
}
