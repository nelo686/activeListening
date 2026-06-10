package com.mrmustard.activelistening.domain.usecase

import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.learning.SectionExplanationProvider
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
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

    fun selectSection(
        sections: List<SongSection>,
        sectionId: Int,
    ): Int? =
        sectionId.takeIf { id -> sections.any { it.id == id } }

    fun openEditor(
        sections: List<SongSection>,
        sectionId: Int,
    ): SectionEditorSelection? {
        val validSectionId = selectSection(sections, sectionId) ?: return null
        return SectionEditorSelection(
            selectedSectionId = validSectionId,
            editingSectionId = validSectionId,
        )
    }

    fun closeEditor(
        selectedSectionId: Int?,
    ): SectionEditorSelection =
        SectionEditorSelection(
            selectedSectionId = selectedSectionId,
            editingSectionId = null,
        )

    fun changeLabel(
        sections: List<SongSection>,
        sectionId: Int,
        label: SectionLabel,
        learningLevel: LearningLevel,
    ): SectionEditingResult {
        val updatedSections = SongStructureEditor.changeLabel(
            sections = sections,
            sectionId = sectionId,
            label = label,
        )
        return SectionEditingResult(
            sections = updatedSections,
            selectedSectionId = sectionId,
            editingSectionId = sectionId,
            learningContent = learningContent(
                sections = updatedSections,
                editingSectionId = sectionId,
                learningLevel = learningLevel,
            ),
        )
    }

    fun cycleStatus(
        sections: List<SongSection>,
        sectionId: Int,
        learningLevel: LearningLevel,
    ): SectionEditingResult {
        val section = sections.firstOrNull { it.id == sectionId }
            ?: return SectionEditingResult(
                sections = sections,
                selectedSectionId = sectionId,
                editingSectionId = sectionId,
                learningContent = learningContent(
                    sections = sections,
                    editingSectionId = sectionId,
                    learningLevel = learningLevel,
                ),
            )
        val nextStatus = when (section.status) {
            SectionStatus.Suggested -> SectionStatus.Confirmed
            SectionStatus.Confirmed -> SectionStatus.Uncertain
            SectionStatus.Uncertain -> SectionStatus.Suggested
        }
        val updatedSections = SongStructureEditor.changeStatus(
            sections = sections,
            sectionId = sectionId,
            status = nextStatus,
        )
        return SectionEditingResult(
            sections = updatedSections,
            selectedSectionId = sectionId,
            editingSectionId = sectionId,
            learningContent = learningContent(
                sections = updatedSections,
                editingSectionId = sectionId,
                learningLevel = learningLevel,
            ),
        )
    }

    fun toggleMusicalContrast(
        sections: List<SongSection>,
        sectionId: Int,
        learningLevel: LearningLevel,
    ): SectionEditingResult {
        val updatedSections = sections.map { section ->
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
        }
        return SectionEditingResult(
            sections = updatedSections,
            selectedSectionId = sectionId,
            editingSectionId = sectionId,
            learningContent = learningContent(
                sections = updatedSections,
                editingSectionId = sectionId,
                learningLevel = learningLevel,
            ),
        )
    }

    fun setBoundary(
        sections: List<SongSection>,
        sectionId: Int,
        boundary: SectionBoundary,
        positionMillis: Long,
        learningLevel: LearningLevel,
    ): SectionEditingResult {
        val updatedSections = SongStructureFactory.setSectionBoundary(
            sections = sections,
            sectionId = sectionId,
            boundary = boundary,
            positionMillis = positionMillis,
        )
        return SectionEditingResult(
            sections = updatedSections,
            selectedSectionId = sectionId,
            editingSectionId = sectionId,
            learningContent = learningContent(
                sections = updatedSections,
                editingSectionId = sectionId,
                learningLevel = learningLevel,
            ),
        )
    }

    fun splitAtPosition(
        sections: List<SongSection>,
        positionMillis: Long,
        learningLevel: LearningLevel,
    ): SectionEditingResult {
        val updatedSections = SongStructureFactory.splitSectionAt(
            sections = sections,
            positionMillis = positionMillis,
        )
        val selectedSectionId = SongStructureFactory.activeSectionId(
            sections = updatedSections,
            positionMillis = positionMillis,
        )
        return SectionEditingResult(
            sections = updatedSections,
            selectedSectionId = selectedSectionId,
            editingSectionId = selectedSectionId,
            learningContent = learningContent(
                sections = updatedSections,
                editingSectionId = selectedSectionId,
                learningLevel = learningLevel,
            ),
        )
    }

    fun removeBoundaryAfter(
        sections: List<SongSection>,
        sectionId: Int,
        learningLevel: LearningLevel,
    ): SectionEditingResult {
        val updatedSections = SongStructureFactory.removeBoundaryAfter(
            sections = sections,
            sectionId = sectionId,
        )
        val selectedSectionId = sectionId.takeIf { id ->
            updatedSections.any { section -> section.id == id }
        }
        return SectionEditingResult(
            sections = updatedSections,
            selectedSectionId = selectedSectionId,
            editingSectionId = selectedSectionId,
            learningContent = learningContent(
                sections = updatedSections,
                editingSectionId = selectedSectionId,
                learningLevel = learningLevel,
            ),
        )
    }

    fun learningContent(
        sections: List<SongSection>,
        editingSectionId: Int?,
        learningLevel: LearningLevel,
    ): SectionLearningContent? {
        val section = sections.firstOrNull { it.id == editingSectionId } ?: return null
        return SectionExplanationProvider.contentFor(
            label = section.label,
            level = learningLevel,
            status = section.status,
        )
    }
}

data class SectionEditorSelection(
    val selectedSectionId: Int?,
    val editingSectionId: Int?,
)

data class SectionEditingResult(
    val sections: List<SongSection>,
    val selectedSectionId: Int?,
    val editingSectionId: Int?,
    val learningContent: SectionLearningContent?,
)

private const val MANUAL_MUSICAL_CONTRAST_EXPLANATION =
    "Cambio marcado manualmente. Comprueba si el contraste esta en el ritmo o la sensacion."
