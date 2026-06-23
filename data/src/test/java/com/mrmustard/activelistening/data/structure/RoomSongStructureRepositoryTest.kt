package com.mrmustard.activelistening.data.structure

import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RoomSongStructureRepositoryTest {

    private val dao = FakeSongStructureDao()
    private val repository = RoomSongStructureRepository(dao)

    @Test
    fun `saves and restores original and edited structure versions`() = runBlocking {
        val original = SongStructureFactory.createInitialSections(durationMillis = 120_000L)
        val edited = original.map { section ->
            if (section.id == 1) {
                section.copy(
                    label = SectionLabel.Other,
                    customLabel = "Pre-coro",
                    status = SectionStatus.Confirmed,
                    isApproximate = false,
                    musicalContrast = SectionMusicalContrast(
                        confidence = SectionRhythmConfidence.Moderate,
                        explanation = "Cambio claro de sensacion ritmica.",
                    ),
                )
            } else {
                section
            }
        }

        repository.saveStructure(
            songKey = "content://song",
            originalSections = original,
            editedSections = edited,
        )

        val restored = repository.getStructure("content://song")

        assertNotNull(restored)
        assertEquals(original, restored?.originalSections)
        assertEquals(SectionLabel.Other, restored?.editedSections?.get(1)?.label)
        assertEquals("Pre-coro", restored?.editedSections?.get(1)?.customLabel)
        assertEquals(SectionStatus.Confirmed, restored?.editedSections?.get(1)?.status)
        assertEquals(false, restored?.editedSections?.get(1)?.isApproximate)
        assertEquals(
            "Cambio claro de sensacion ritmica.",
            restored?.editedSections?.get(1)?.musicalContrast?.explanation,
        )
    }

    @Test
    fun `returns null when no structure exists for song`() = runBlocking {
        assertNull(repository.getStructure("content://missing"))
    }

    @Test
    fun `deletes structure for song`() = runBlocking {
        val sections = SongStructureFactory.createInitialSections(120_000L)
        repository.saveStructure("content://song", sections, sections)

        repository.deleteStructure("content://song")

        assertNull(repository.getStructure("content://song"))
    }
}

private class FakeSongStructureDao : SongStructureDao {
    private val sections = mutableListOf<SongStructureSectionEntity>()

    override suspend fun getSections(songKey: String): List<SongStructureSectionEntity> =
        sections.filter { it.songKey == songKey }

    override suspend fun deleteSections(songKey: String) {
        sections.removeAll { it.songKey == songKey }
    }

    override suspend fun insertSections(sections: List<SongStructureSectionEntity>) {
        this.sections += sections
    }

    override suspend fun replaceStructure(
        songKey: String,
        sections: List<SongStructureSectionEntity>,
    ) {
        deleteSections(songKey)
        insertSections(sections)
    }
}
