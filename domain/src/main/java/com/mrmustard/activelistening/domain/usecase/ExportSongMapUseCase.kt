package com.mrmustard.activelistening.domain.usecase

import com.mrmustard.activelistening.domain.export.SongMapExportFactory
import com.mrmustard.activelistening.domain.export.SongMapExportRepository
import com.mrmustard.activelistening.domain.export.SongMapExportResult
import com.mrmustard.activelistening.domain.export.SongMapExportValidation
import com.mrmustard.activelistening.domain.export.SongMapExportValidator
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.structure.SongSection
import javax.inject.Inject

class ExportSongMapUseCase @Inject constructor(
    private val repository: SongMapExportRepository,
) {
    suspend operator fun invoke(
        destination: String,
        song: ImportedSong?,
        sections: List<SongSection>,
        learningLevel: LearningLevel,
    ): ExportSongMapResult {
        if (SongMapExportValidator.validate(song, sections) ==
            SongMapExportValidation.InsufficientStructure
        ) {
            return ExportSongMapResult.InsufficientStructure
        }
        val map = SongMapExportFactory.create(requireNotNull(song), sections, learningLevel)
        return when (repository.exportPdf(destination, map)) {
            SongMapExportResult.Success -> ExportSongMapResult.Success(map.song.displayName)
            SongMapExportResult.UnableToWrite -> ExportSongMapResult.UnableToWrite
        }
    }
}

sealed interface ExportSongMapResult {
    data class Success(val displayName: String) : ExportSongMapResult
    data object InsufficientStructure : ExportSongMapResult
    data object UnableToWrite : ExportSongMapResult
}
