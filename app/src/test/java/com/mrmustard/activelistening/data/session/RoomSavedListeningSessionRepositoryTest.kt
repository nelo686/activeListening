package com.mrmustard.activelistening.data.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RoomSavedListeningSessionRepositoryTest {

    private val dao = FakeSavedListeningSessionDao()
    private val repository = RoomSavedListeningSessionRepository(dao)

    @Test
    fun `saves and restores imported song metadata`() = runBlocking {
        repository.upsertSession(
            songKey = "content://song",
            displayName = "Practice.mp3",
            title = "Practice",
            artist = "The Band",
            mimeType = "audio/mpeg",
            durationMillis = 120_000L,
        )

        val restored = repository.getSession("content://song")

        assertNotNull(restored)
        assertEquals("Practice.mp3", restored?.displayName)
        assertEquals("Practice", restored?.title)
        assertEquals("The Band", restored?.artist)
        assertEquals("audio/mpeg", restored?.mimeType)
        assertEquals(120_000L, restored?.durationMillis)
        assertEquals(0L, restored?.lastPositionMillis)
    }

    @Test
    fun `updates playback position without losing metadata`() = runBlocking {
        repository.upsertSession(
            songKey = "content://song",
            displayName = "Practice.mp3",
            title = "Practice",
            artist = "The Band",
            mimeType = "audio/mpeg",
            durationMillis = 120_000L,
        )

        repository.updatePlaybackPosition(
            songKey = "content://song",
            positionMillis = 42_000L,
        )

        val restored = repository.getSession("content://song")

        assertEquals("Practice.mp3", restored?.displayName)
        assertEquals(42_000L, restored?.lastPositionMillis)
    }

    @Test
    fun `resetting playback to start replaces previous position`() = runBlocking {
        repository.upsertSession(
            songKey = "content://song",
            displayName = "Practice.mp3",
            title = "Practice",
            artist = "The Band",
            mimeType = "audio/mpeg",
            durationMillis = 120_000L,
        )

        repository.updatePlaybackPosition("content://song", 42_000L)
        repository.updatePlaybackPosition("content://song", 0L)

        val restored = repository.getSession("content://song")

        assertEquals(0L, restored?.lastPositionMillis)
    }

    @Test
    fun `upserting existing session keeps last playback position`() = runBlocking {
        repository.upsertSession(
            songKey = "content://song",
            displayName = "Practice.mp3",
            title = "Practice",
            artist = "The Band",
            mimeType = "audio/mpeg",
            durationMillis = 120_000L,
        )

        repository.updatePlaybackPosition("content://song", 30_000L)
        repository.upsertSession(
            songKey = "content://song",
            displayName = "Practice renamed.mp3",
            title = "Practice renamed",
            artist = "The Band",
            mimeType = "audio/mpeg",
            durationMillis = 120_000L,
        )

        val restored = repository.getSession("content://song")

        assertEquals("Practice renamed.mp3", restored?.displayName)
        assertEquals(30_000L, restored?.lastPositionMillis)
    }

    @Test
    fun `deletes and restores exact saved session`() = runBlocking {
        val session = SavedListeningSessionEntity(
            songKey = "content://song",
            displayName = "Practice.mp3",
            title = "Practice",
            artist = "The Band",
            mimeType = "audio/mpeg",
            durationMillis = 120_000L,
            lastPositionMillis = 42_000L,
            createdAtMillis = 10L,
            updatedAtMillis = 20L,
        )
        dao.upsertSession(session)

        repository.deleteSession(session.songKey)
        assertEquals(null, repository.getSession(session.songKey))

        repository.restoreSession(
            com.mrmustard.activelistening.domain.session.SavedListeningSession(
                songKey = session.songKey,
                displayName = session.displayName,
                title = session.title,
                artist = session.artist,
                mimeType = session.mimeType,
                durationMillis = session.durationMillis,
                lastPositionMillis = session.lastPositionMillis,
                createdAtMillis = session.createdAtMillis,
                updatedAtMillis = session.updatedAtMillis,
            ),
        )

        val restored = repository.getSession(session.songKey)
        assertEquals(42_000L, restored?.lastPositionMillis)
        assertEquals(10L, restored?.createdAtMillis)
        assertEquals(20L, restored?.updatedAtMillis)
    }
}

private class FakeSavedListeningSessionDao : SavedListeningSessionDao {
    private val sessions = mutableListOf<SavedListeningSessionEntity>()
    private val sessionsFlow = MutableStateFlow<List<SavedListeningSessionEntity>>(emptyList())

    override fun observeSessions(): Flow<List<SavedListeningSessionEntity>> = sessionsFlow

    override suspend fun getSession(songKey: String): SavedListeningSessionEntity? =
        sessions.firstOrNull { it.songKey == songKey }

    override suspend fun upsertSession(session: SavedListeningSessionEntity) {
        sessions.removeAll { it.songKey == session.songKey }
        sessions += session
        sessionsFlow.value = sessions.sortedByDescending { it.updatedAtMillis }
    }

    override suspend fun deleteSession(songKey: String) {
        sessions.removeAll { it.songKey == songKey }
        sessionsFlow.value = sessions.sortedByDescending { it.updatedAtMillis }
    }

    override suspend fun updatePlaybackPosition(
        songKey: String,
        positionMillis: Long,
        updatedAtMillis: Long,
    ) {
        val index = sessions.indexOfFirst { it.songKey == songKey }
        if (index == -1) return
        sessions[index] = sessions[index].copy(
            lastPositionMillis = positionMillis,
            updatedAtMillis = updatedAtMillis,
        )
        sessionsFlow.value = sessions.sortedByDescending { it.updatedAtMillis }
    }
}
