package com.mrmustard.activelistening.domain.importsong

interface SongImportGateway {
    suspend fun importSong(uri: String): SongImportResult
}
