package com.mrmustard.activelistening.domain.export

import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.learning.SectionExplanationProvider
import com.mrmustard.activelistening.domain.structure.SongSection

data class SongMapExport(
    val song: ImportedSong,
    val sections: List<ExportSection>,
)

data class ExportSection(
    val index: Int,
    val section: SongSection,
    val educationalNote: String,
)

object SongMapExportFactory {

    fun create(
        song: ImportedSong,
        sections: List<SongSection>,
        learningLevel: LearningLevel,
    ): SongMapExport =
        SongMapExport(
            song = song,
            sections = sections.mapIndexed { index, section ->
                ExportSection(
                    index = index + 1,
                    section = section,
                    educationalNote = SectionExplanationProvider.contentFor(
                        label = section.label,
                        level = learningLevel,
                        status = section.status,
                    ).summary,
                )
            },
        )
}
