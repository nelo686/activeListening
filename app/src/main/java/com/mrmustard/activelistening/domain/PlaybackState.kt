package com.mrmustard.activelistening.domain

data class PlaybackState(
    val isReady: Boolean = false,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val errorMessage: String? = null,
)
