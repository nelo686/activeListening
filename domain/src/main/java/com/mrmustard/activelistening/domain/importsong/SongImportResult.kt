package com.mrmustard.activelistening.domain.importsong

sealed interface SongImportResult {
    data class Success(val song: ImportedSong) : SongImportResult
    data class Error(val error: ImportSongError) : SongImportResult
}