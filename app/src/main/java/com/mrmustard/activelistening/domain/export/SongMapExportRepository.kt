package com.mrmustard.activelistening.domain.export

import android.net.Uri

interface SongMapExportRepository {
    suspend fun exportPdf(
        destination: Uri,
        map: SongMapExport,
    ): SongMapExportResult
}
