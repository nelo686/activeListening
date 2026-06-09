package com.mrmustard.activelistening.data.structure

import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import com.mrmustard.activelistening.domain.structure.SectionRhythmEstimator
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureMap
import com.mrmustard.activelistening.domain.structure.SongStructureRepository
import javax.inject.Inject

class RoomSongStructureRepository @Inject constructor(
    private val dao: SongStructureDao,
) : SongStructureRepository {

    override suspend fun getStructure(songKey: String): SongStructureMap? {
        val entities = dao.getSections(songKey)
        if (entities.isEmpty()) return null

        val originalSections = entities
            .filter { it.version == SongStructureVersion.Original.name }
            .toDomainSections()
        val editedSections = entities
            .filter { it.version == SongStructureVersion.Edited.name }
            .toDomainSections()

        if (originalSections.isEmpty() || editedSections.isEmpty()) return null
        return SongStructureMap(
            originalSections = originalSections,
            editedSections = editedSections,
        )
    }

    override suspend fun saveStructure(
        songKey: String,
        originalSections: List<SongSection>,
        editedSections: List<SongSection>,
    ) {
        dao.replaceStructure(
            songKey = songKey,
            sections = originalSections.toEntities(songKey, SongStructureVersion.Original) +
                editedSections.toEntities(songKey, SongStructureVersion.Edited),
        )
    }

    private fun List<SongSection>.toEntities(
        songKey: String,
        version: SongStructureVersion,
    ): List<SongStructureSectionEntity> =
        mapIndexed { index, section ->
            SongStructureSectionEntity(
                songKey = songKey,
                version = version.name,
                sectionId = section.id,
                position = index,
                startMillis = section.startMillis,
                endMillis = section.endMillis,
                label = section.label.name,
                status = section.status.name,
                prompt = section.prompt,
                isApproximate = section.isApproximate,
                musicalContrastConfidence = section.musicalContrast?.confidence?.name,
                musicalContrastExplanation = section.musicalContrast?.explanation,
            )
        }

    private fun List<SongStructureSectionEntity>.toDomainSections(): List<SongSection> =
        sortedBy { it.position }.map { entity ->
            SongSection(
                id = entity.sectionId,
                startMillis = entity.startMillis,
                endMillis = entity.endMillis,
                label = enumValueOrDefault(entity.label, SectionLabel.Other),
                status = enumValueOrDefault(entity.status, SectionStatus.Suggested),
                prompt = entity.prompt,
                isApproximate = entity.isApproximate,
                rhythmInfo = SectionRhythmEstimator.estimate(entity.endMillis - entity.startMillis),
                musicalContrast = entity.toMusicalContrast(),
            )
        }

    private fun SongStructureSectionEntity.toMusicalContrast(): SectionMusicalContrast? {
        val confidence = musicalContrastConfidence?.let {
            enumValueOrDefault(it, SectionRhythmConfidence.Low)
        } ?: return null
        val explanation = musicalContrastExplanation ?: return null
        return SectionMusicalContrast(
            confidence = confidence,
            explanation = explanation,
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(
        value: String,
        default: T,
    ): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default
}
