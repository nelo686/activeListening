package com.mrmustard.activelistening.domain.export

import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SongMapExportValidatorTest {

    @Test
    fun `rejects empty map`() {
        val result = SongMapExportValidator.validate(
            durationMillis = 120_000L,
            sections = emptyList(),
        )

        assertEquals(SongMapExportValidation.InsufficientStructure, result)
    }

    @Test
    fun `allows coherent section map`() {
        val sections = SongStructureFactory.createInitialSections(120_000L)

        val result = SongMapExportValidator.validate(
            durationMillis = 120_000L,
            sections = sections,
        )

        assertEquals(SongMapExportValidation.Valid, result)
        assertTrue(result == SongMapExportValidation.Valid)
    }

    @Test
    fun `rejects sections outside song duration`() {
        val sections = SongStructureFactory.createInitialSections(120_000L)

        val result = SongMapExportValidator.validate(
            durationMillis = 60_000L,
            sections = sections,
        )

        assertEquals(SongMapExportValidation.InsufficientStructure, result)
    }
}
