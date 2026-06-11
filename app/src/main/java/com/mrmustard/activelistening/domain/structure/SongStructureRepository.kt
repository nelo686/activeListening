package com.mrmustard.activelistening.domain.structure

interface SongStructureRepository {
    suspend fun getStructure(songKey: String): SongStructureMap?

    suspend fun saveStructure(
        songKey: String,
        originalSections: List<SongSection>,
        editedSections: List<SongSection>,
    )

    suspend fun deleteStructure(songKey: String)
}
