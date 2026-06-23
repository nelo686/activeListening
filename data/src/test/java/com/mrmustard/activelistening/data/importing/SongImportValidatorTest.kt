package com.mrmustard.activelistening.data.importing

import com.mrmustard.activelistening.domain.importsong.ImportSongError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongImportValidatorTest {

    private val validator = SongImportValidator()

    @Test
    fun `accepts supported mime type`() {
        val result = validator.validateFormat(
            mimeType = "audio/mpeg",
            displayName = "song.bin",
        )

        assertNull(result)
    }

    @Test
    fun `accepts supported extension when mime type is missing`() {
        val result = validator.validateFormat(
            mimeType = null,
            displayName = "song.wav",
        )

        assertNull(result)
    }

    @Test
    fun `rejects unsupported format`() {
        val result = validator.validateFormat(
            mimeType = "application/pdf",
            displayName = "notes.pdf",
        )

        assertEquals(ImportSongError.UnsupportedFormat, result)
    }

    @Test
    fun `rejects songs longer than fifteen minutes`() {
        val result = validator.validateDuration(
            durationMillis = SongImportValidator.MAX_DURATION_MILLIS + 1L,
        )

        assertEquals(
            ImportSongError.TooLong(SongImportValidator.MAX_DURATION_MILLIS),
            result,
        )
    }

    @Test
    fun `accepts songs at duration limit`() {
        val result = validator.validateDuration(
            durationMillis = SongImportValidator.MAX_DURATION_MILLIS,
        )

        assertNull(result)
    }
}
