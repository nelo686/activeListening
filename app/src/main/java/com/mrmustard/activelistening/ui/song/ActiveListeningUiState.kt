package com.mrmustard.activelistening.ui.song

import com.mrmustard.activelistening.domain.importsong.ImportSongError
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.export.SongMapExportValidator
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.progress.LearningProgressSummary

data class ActiveListeningUiState(
    val isImporting: Boolean = false,
    val importedSong: ImportedSong? = null,
    val importError: ImportSongError? = null,
    val savedSessions: List<SavedListeningSession> = emptyList(),
    val savedSongArtwork: Map<String, ByteArray?> = emptyMap(),
    val progressSummaries: Map<String, LearningProgressSummary> = emptyMap(),
    val savedSessionDeletionEvent: SavedSessionDeletionEvent? = null,
    val playbackState: PlaybackState = PlaybackState(),
    val isGuidedSessionActive: Boolean = false,
    val isGuidanceLoading: Boolean = false,
    val guidanceError: GuidanceError? = null,
    val sections: List<SongSection> = emptyList(),
    val originalSections: List<SongSection> = emptyList(),
    val selectedSectionId: Int? = null,
    val activeSectionId: Int? = null,
    val editingSectionId: Int? = null,
    val guidanceIntensity: GuidanceIntensity = GuidanceIntensity.Normal,
    val learningLevel: LearningLevel = LearningLevel.Introductory,
    val editingSectionLearningContent: SectionLearningContent? = null,
    val isExportingMap: Boolean = false,
    val mapExportError: MapExportError? = null,
    val exportedMapFileName: String? = null,
) {
    val canRestoreOriginalProposal: Boolean
        get() = originalSections.isNotEmpty() && sections != originalSections

    val canExportMap: Boolean
        get() = SongMapExportValidator.canExport(importedSong, sections)
}

enum class GuidanceError {
    MissingApiKey,
    UnableToGenerate,
}

enum class MapExportError {
    InsufficientStructure,
    UnableToWrite,
}

data class SavedSessionDeletionEvent(
    val id: Long,
    val deletedDisplayName: String? = null,
) {
    val isError: Boolean
        get() = deletedDisplayName == null
}
