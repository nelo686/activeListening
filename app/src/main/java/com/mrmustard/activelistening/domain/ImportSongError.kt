package com.mrmustard.activelistening.domain

sealed interface ImportSongError {
    data object UnsupportedFormat : ImportSongError
    data object UnreadableFile : ImportSongError
    data class TooLong(val maxDurationMillis: Long) : ImportSongError
}
