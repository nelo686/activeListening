package com.mrmustard.activelistening.domain.export

sealed interface SongMapExportResult {
    data object Success : SongMapExportResult
    data object UnableToWrite : SongMapExportResult
}
