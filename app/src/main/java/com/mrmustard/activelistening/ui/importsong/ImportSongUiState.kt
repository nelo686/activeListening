package com.mrmustard.activelistening.ui.importsong

import com.mrmustard.activelistening.domain.ImportedSong
import com.mrmustard.activelistening.domain.PlaybackState

data class ImportSongUiState(
    val isImporting: Boolean = false,
    val importedSong: ImportedSong? = null,
    val importErrorMessage: String? = null,
    val playbackState: PlaybackState = PlaybackState(),
)
