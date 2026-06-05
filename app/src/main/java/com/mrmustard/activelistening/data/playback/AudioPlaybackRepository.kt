package com.mrmustard.activelistening.data.playback

import com.mrmustard.activelistening.domain.ImportedSong
import com.mrmustard.activelistening.domain.PlaybackState
import kotlinx.coroutines.flow.StateFlow

interface AudioPlaybackRepository {
    val playbackState: StateFlow<PlaybackState>

    fun load(song: ImportedSong)
    fun play()
    fun pause()
    fun seekTo(positionMillis: Long)
    fun release()
}
