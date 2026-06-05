package com.mrmustard.activelistening.data.importing

import android.net.Uri
import com.mrmustard.activelistening.domain.SongImportResult

interface SongImportRepository {
    suspend fun importSong(uri: Uri): SongImportResult
}
