package com.mrmustard.activelistening.domain.usecase

import com.mrmustard.activelistening.domain.structure.SectionBoundary
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureEditor
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import javax.inject.Inject

class SectionEditingUseCase @Inject constructor() {

    fun changeLabel(
        sections: List<SongSection>,
        sectionId: Int,
        label: SectionLabel,
    ): SectionEditingResult = result(
        SongStructureEditor.changeLabel(sections, sectionId, label),
        sectionId,
    )

    fun changeCustomLabel(
        sections: List<SongSection>,
        sectionId: Int,
        customLabel: String,
    ): SectionEditingResult = result(
        SongStructureEditor.changeCustomLabel(sections, sectionId, customLabel),
        sectionId,
    )

    fun changeStatus(
        sections: List<SongSection>,
        sectionId: Int,
        status: SectionStatus,
    ): SectionEditingResult = result(
        SongStructureEditor.changeStatus(sections, sectionId, status),
        sectionId,
    )

    fun cycleStatus(
        sections: List<SongSection>,
        sectionId: Int,
    ): SectionEditingResult {
        val section = sections.firstOrNull { it.id == sectionId }
            ?: return result(sections, sectionId)
        val nextStatus = when (section.status) {
            SectionStatus.Suggested -> SectionStatus.Confirmed
            SectionStatus.Confirmed -> SectionStatus.Uncertain
            SectionStatus.Uncertain -> SectionStatus.Suggested
        }
        return changeStatus(sections, sectionId, nextStatus)
    }

    fun toggleMusicalContrast(
        sections: List<SongSection>,
        sectionId: Int,
    ): SectionEditingResult = result(
        sections.map { section ->
            if (section.id != sectionId) {
                section
            } else {
                section.copy(
                    musicalContrast = if (section.musicalContrast == null) {
                        SectionMusicalContrast(
                            confidence = SectionRhythmConfidence.Low,
                            explanation = MANUAL_MUSICAL_CONTRAST_EXPLANATION,
                        )
                    } else {
                        null
                    },
                )
            }
        },
        sectionId,
    )

    fun setBoundary(
        sections: List<SongSection>,
        sectionId: Int,
        boundary: SectionBoundary,
        positionMillis: Long,
    ): SectionEditingResult = result(
        SongStructureFactory.setSectionBoundary(sections, sectionId, boundary, positionMillis),
        sectionId,
    )

    fun splitAtPosition(
        sections: List<SongSection>,
        positionMillis: Long,
    ): SectionEditingResult {
        val updatedSections = SongStructureFactory.splitSectionAt(sections, positionMillis)
        return result(
            sections = updatedSections,
            selectedSectionId = SongStructureFactory.activeSectionId(updatedSections, positionMillis),
        )
    }

    fun removeBoundaryAfter(
        sections: List<SongSection>,
        sectionId: Int,
    ): SectionEditingResult {
        val updatedSections = SongStructureFactory.removeBoundaryAfter(sections, sectionId)
        return result(
            sections = updatedSections,
            selectedSectionId = sectionId.takeIf { id -> updatedSections.any { it.id == id } },
        )
    }

    private fun result(
        sections: List<SongSection>,
        selectedSectionId: Int?,
    ) = SectionEditingResult(
        sections = sections,
        selectedSectionId = selectedSectionId,
    )
}

data class SectionEditingResult(
    val sections: List<SongSection>,
    val selectedSectionId: Int?,
)

private const val MANUAL_MUSICAL_CONTRAST_EXPLANATION =
    "Cambio marcado manualmente. Comprueba si el contraste está en el ritmo o la sensación."
