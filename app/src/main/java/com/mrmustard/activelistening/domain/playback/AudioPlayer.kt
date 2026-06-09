package com.mrmustard.activelistening.domain.playback

import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val playbackState: StateFlow<PlaybackState>

    fun load(song: ImportedSong)
    fun play()
    fun pause()
    fun seekTo(positionMillis: Long)
    fun release()
}
