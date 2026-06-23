package com.mrmustard.activelistening.domain.session

interface SongArtworkRepository {
    suspend fun load(songKey: String): ByteArray?
}
