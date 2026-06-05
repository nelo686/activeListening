package com.mrmustard.activelistening.data.importing

import com.mrmustard.activelistening.domain.ImportSongError
import javax.inject.Inject

class SongImportValidator @Inject constructor() {
    fun validateFormat(mimeType: String?, displayName: String): ImportSongError? {
        val normalizedMimeType = mimeType?.lowercase()
        val normalizedName = displayName.lowercase()

        val supportedByMime = normalizedMimeType in SUPPORTED_MIME_TYPES
        val supportedByExtension = SUPPORTED_EXTENSIONS.any { normalizedName.endsWith(it) }

        return if (supportedByMime || supportedByExtension) {
            null
        } else {
            ImportSongError.UnsupportedFormat
        }
    }

    fun validateDuration(durationMillis: Long): ImportSongError? =
        if (durationMillis > MAX_DURATION_MILLIS) {
            ImportSongError.TooLong(MAX_DURATION_MILLIS)
        } else {
            null
        }

    companion object {
        const val MAX_DURATION_MILLIS: Long = 15 * 60 * 1000L
        val SUPPORTED_FORMATS = listOf("MP3", "WAV", "M4A", "AAC")

        private val SUPPORTED_EXTENSIONS = setOf(".mp3", ".wav", ".m4a", ".aac")
        private val SUPPORTED_MIME_TYPES = setOf(
            "audio/aac",
            "audio/aacp",
            "audio/mp4",
            "audio/mpeg",
            "audio/wav",
            "audio/x-m4a",
            "audio/x-wav",
            "audio/vnd.wave",
        )
    }
}
