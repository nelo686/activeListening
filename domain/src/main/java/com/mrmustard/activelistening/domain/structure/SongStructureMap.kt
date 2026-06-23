package com.mrmustard.activelistening.domain.structure

data class SongStructureMap(
    val originalSections: List<SongSection>,
    val editedSections: List<SongSection>,
)
