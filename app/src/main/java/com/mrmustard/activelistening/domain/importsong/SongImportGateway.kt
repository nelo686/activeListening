package com.mrmustard.activelistening.domain.importsong

import android.net.Uri

interface SongImportGateway {
    suspend fun importSong(uri: Uri): SongImportResult
}
