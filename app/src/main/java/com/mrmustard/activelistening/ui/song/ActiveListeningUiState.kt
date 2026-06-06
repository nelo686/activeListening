package com.mrmustard.activelistening.ui.song

import com.mrmustard.activelistening.domain.ImportSongError
import com.mrmustard.activelistening.domain.ImportedSong
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.ui.song.guide.GuidedListeningMarker

data class ActiveListeningUiState(
    val isImporting: Boolean = false,
    val importedSong: ImportedSong? = null,
    val importError: ImportSongError? = null,
    val playbackState: PlaybackState = PlaybackState(),
    val isGuidedSessionActive: Boolean = false,
    val isGuidanceLoading: Boolean = false,
    val guidanceError: GuidanceError? = null,
    val guidedTimeline: List<GuidedListeningMarker> = emptyList(),
    val currentGuidedMarker: GuidedListeningMarker? = null,
)

enum class GuidanceError {
    MissingApiKey,
    UnableToGenerate,
}
