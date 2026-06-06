package com.mrmustard.activelistening.ui.song.importsong

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.data.importing.SongImportValidator
import com.mrmustard.activelistening.domain.ImportSongError

@Composable
fun ImportSongError.toMessage(): String =
    when (this) {
        ImportSongError.UnsupportedFormat -> stringResource(
            R.string.import_song_error_unsupported_format,
            SongImportValidator.SUPPORTED_FORMATS.joinToString(", "),
        )

        ImportSongError.UnreadableFile -> stringResource(R.string.import_song_error_unreadable_file)

        is ImportSongError.TooLong -> stringResource(
            R.string.import_song_error_too_long,
            maxDurationMillis.toMinutes(),
        )
    }

private fun Long.toMinutes(): Long = this / 60_000L
