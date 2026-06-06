package com.mrmustard.activelistening.ui.song

import com.mrmustard.activelistening.domain.ImportSongError
import com.mrmustard.activelistening.domain.ImportedSong
import com.mrmustard.activelistening.domain.PlaybackState
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
    val isGuidanceReduced: Boolean = false,
)

enum class GuidanceError {
    MissingApiKey,
    UnableToGenerate,
}
