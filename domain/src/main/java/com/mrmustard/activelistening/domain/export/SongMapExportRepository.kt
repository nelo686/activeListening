package com.mrmustard.activelistening.domain.export

interface SongMapExportRepository {
    suspend fun exportPdf(
        destination: String,
        map: SongMapExport,
    ): SongMapExportResult
}
