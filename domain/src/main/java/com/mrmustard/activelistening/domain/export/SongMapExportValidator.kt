package com.mrmustard.activelistening.domain.export

import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.structure.SongSection

object SongMapExportValidator {

    fun canExport(
        song: ImportedSong?,
        sections: List<SongSection>,
    ): Boolean = validate(song, sections) == SongMapExportValidation.Valid

    fun validate(
        song: ImportedSong?,
        sections: List<SongSection>,
    ): SongMapExportValidation =
        validate(
            durationMillis = song?.durationMillis,
            sections = sections,
        )

    fun validate(
        durationMillis: Long?,
        sections: List<SongSection>,
    ): SongMapExportValidation {
        if (durationMillis == null || durationMillis <= 0L || sections.isEmpty()) {
            return SongMapExportValidation.InsufficientStructure
        }
        val hasInvalidSection = sections.any { section ->
            section.endMillis <= section.startMillis ||
                section.startMillis < 0L ||
                section.endMillis > durationMillis
        }
        return if (hasInvalidSection) {
            SongMapExportValidation.InsufficientStructure
        } else {
            SongMapExportValidation.Valid
        }
    }
}

enum class SongMapExportValidation {
    Valid,
    InsufficientStructure,
}
