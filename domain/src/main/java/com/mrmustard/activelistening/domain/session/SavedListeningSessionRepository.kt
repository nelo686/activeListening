package com.mrmustard.activelistening.domain.session

import com.mrmustard.activelistening.domain.importsong.ImportedSong
import kotlinx.coroutines.flow.Flow

interface SavedListeningSessionRepository {
    val sessions: Flow<List<SavedListeningSession>>

    suspend fun getSession(songKey: String): SavedListeningSession?

    suspend fun upsertSession(song: ImportedSong)

    suspend fun restoreSession(session: SavedListeningSession)

    suspend fun deleteSession(songKey: String)

    suspend fun updatePlaybackPosition(
        songKey: String,
        positionMillis: Long,
    )
}
