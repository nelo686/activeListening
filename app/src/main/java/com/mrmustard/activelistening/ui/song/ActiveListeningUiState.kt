package com.mrmustard.activelistening.ui.song

import com.mrmustard.activelistening.domain.importsong.ImportSongError
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
import com.mrmustard.activelistening.domain.structure.SongSection

data class ActiveListeningUiState(
    val isImporting: Boolean = false,
    val importedSong: ImportedSong? = null,
    val importError: ImportSongError? = null,
    val playbackState: PlaybackState = PlaybackState(),
    val isGuidedSessionActive: Boolean = false,
    val isGuidanceLoading: Boolean = false,
    val guidanceError: GuidanceError? = null,
    val sections: List<SongSection> = emptyList(),
    val selectedSectionId: Int? = null,
    val activeSectionId: Int? = null,
    val editingSectionId: Int? = null,
    val guidanceIntensity: GuidanceIntensity = GuidanceIntensity.Normal,
    val learningLevel: LearningLevel = LearningLevel.Introductory,
    val editingSectionLearningContent: SectionLearningContent? = null,
)

enum class GuidanceError {
    MissingApiKey,
    UnableToGenerate,
}
