package com.mrmustard.activelistening.domain

sealed interface PlaybackError {
    data object UnableToPlay : PlaybackError
}
