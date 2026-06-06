package com.mrmustard.activelistening.domain.structure

object SongStructureEditor {

    fun changeLabel(
        sections: List<SongSection>,
        sectionId: Int,
        label: SectionLabel,
    ): List<SongSection> =
        sections.updateSection(sectionId) { section ->
            section.copy(label = label)
        }

    fun changeStatus(
        sections: List<SongSection>,
        sectionId: Int,
        status: SectionStatus,
    ): List<SongSection> =
        sections.updateSection(sectionId) { section ->
            section.copy(
                status = status,
                isApproximate = if (status == SectionStatus.Confirmed) false else section.isApproximate,
            )
        }

    private fun List<SongSection>.updateSection(
        sectionId: Int,
        transform: (SongSection) -> SongSection,
    ): List<SongSection> =
        map { section ->
            if (section.id == sectionId) transform(section) else section
        }
}
