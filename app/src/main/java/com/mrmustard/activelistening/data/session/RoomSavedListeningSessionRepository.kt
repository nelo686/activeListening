package com.mrmustard.activelistening.data.session

import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.session.SavedListeningSessionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSavedListeningSessionRepository @Inject constructor(
    private val dao: SavedListeningSessionDao,
) : SavedListeningSessionRepository {

    override val sessions: Flow<List<SavedListeningSession>> =
        dao.observeSessions().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getSession(songKey: String): SavedListeningSession? =
        dao.getSession(songKey)?.toDomain()

    override suspend fun upsertSession(song: ImportedSong) {
        upsertSession(
            songKey = song.uri.toString(),
            displayName = song.displayName,
            title = song.title,
            artist = song.artist,
            mimeType = song.mimeType,
            durationMillis = song.durationMillis,
        )
    }

    override suspend fun restoreSession(session: SavedListeningSession) {
        dao.upsertSession(session.toEntity())
    }

    override suspend fun deleteSession(songKey: String) {
        dao.deleteSession(songKey)
    }

    suspend fun upsertSession(
        songKey: String,
        displayName: String,
        title: String?,
        artist: String?,
        mimeType: String?,
        durationMillis: Long,
    ) {
        val existingSession = dao.getSession(songKey)
        val nowMillis = System.currentTimeMillis()
        dao.upsertSession(
            SavedListeningSessionEntity(
                songKey = songKey,
                displayName = displayName,
                title = title,
                artist = artist,
                mimeType = mimeType,
                durationMillis = durationMillis,
                lastPositionMillis = existingSession?.lastPositionMillis ?: 0L,
                createdAtMillis = existingSession?.createdAtMillis ?: nowMillis,
                updatedAtMillis = nowMillis,
            ),
        )
    }

    override suspend fun updatePlaybackPosition(
        songKey: String,
        positionMillis: Long,
    ) {
        dao.updatePlaybackPosition(
            songKey = songKey,
            positionMillis = positionMillis.coerceAtLeast(0L),
            updatedAtMillis = System.currentTimeMillis(),
        )
    }

    private fun SavedListeningSessionEntity.toDomain(): SavedListeningSession =
        SavedListeningSession(
            songKey = songKey,
            displayName = displayName,
            title = title,
            artist = artist,
            mimeType = mimeType,
            durationMillis = durationMillis,
            lastPositionMillis = lastPositionMillis,
            createdAtMillis = createdAtMillis,
            updatedAtMillis = updatedAtMillis,
        )

    private fun SavedListeningSession.toEntity(): SavedListeningSessionEntity =
        SavedListeningSessionEntity(
            songKey = songKey,
            displayName = displayName,
            title = title,
            artist = artist,
            mimeType = mimeType,
            durationMillis = durationMillis,
            lastPositionMillis = lastPositionMillis,
            createdAtMillis = createdAtMillis,
            updatedAtMillis = updatedAtMillis,
        )
}
