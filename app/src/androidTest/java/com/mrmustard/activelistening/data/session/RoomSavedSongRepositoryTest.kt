package com.mrmustard.activelistening.data.session

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mrmustard.activelistening.data.local.ActiveListeningDatabase
import com.mrmustard.activelistening.data.structure.RoomSongStructureRepository
import com.mrmustard.activelistening.data.progress.RoomLearningProgressRepository
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomSavedSongRepositoryTest {

    private lateinit var database: ActiveListeningDatabase
    private lateinit var sessionRepository: RoomSavedListeningSessionRepository
    private lateinit var structureRepository: RoomSongStructureRepository
    private lateinit var progressRepository: RoomLearningProgressRepository
    private lateinit var repository: RoomSavedSongRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ActiveListeningDatabase::class.java,
        ).allowMainThreadQueries().build()
        sessionRepository = RoomSavedListeningSessionRepository(database.savedListeningSessionDao())
        structureRepository = RoomSongStructureRepository(database.songStructureDao())
        progressRepository = RoomLearningProgressRepository(database.learningProgressDao())
        repository = RoomSavedSongRepository(
            database,
            sessionRepository,
            structureRepository,
            progressRepository,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deleteAndRestorePreservesSessionAndStructure() = runBlocking {
        val session = savedSession("content://song/one", "One.mp3")
        val sections = SongStructureFactory.createInitialSections(session.durationMillis)
        sessionRepository.restoreSession(session)
        structureRepository.saveStructure(session.songKey, sections, sections)

        val deletedSong = repository.deleteSavedSong(session.songKey)

        assertNotNull(deletedSong)
        assertNull(sessionRepository.getSession(session.songKey))
        assertNull(structureRepository.getStructure(session.songKey))

        repository.restoreSavedSong(requireNotNull(deletedSong))

        assertEquals(session, sessionRepository.getSession(session.songKey))
        assertEquals(sections, structureRepository.getStructure(session.songKey)?.editedSections)
    }

    @Test
    fun deletingSongDoesNotAffectOtherSongs() = runBlocking {
        val first = savedSession("content://song/one", "One.mp3")
        val second = savedSession("content://song/two", "Two.mp3")
        sessionRepository.restoreSession(first)
        sessionRepository.restoreSession(second)

        repository.deleteSavedSong(first.songKey)

        assertNull(sessionRepository.getSession(first.songKey))
        assertEquals(second, sessionRepository.getSession(second.songKey))
    }

    @Test
    fun deletingMissingSongDoesNotChangeStoredData() = runBlocking {
        val stored = savedSession("content://song/one", "One.mp3")
        sessionRepository.restoreSession(stored)

        val deletedSong = repository.deleteSavedSong("content://song/missing")

        assertNull(deletedSong)
        assertEquals(stored, sessionRepository.getSession(stored.songKey))
    }

    private fun savedSession(songKey: String, displayName: String): SavedListeningSession =
        SavedListeningSession(
            songKey = songKey,
            displayName = displayName,
            mimeType = "audio/mpeg",
            durationMillis = 120_000L,
            lastPositionMillis = 42_000L,
            createdAtMillis = 10L,
            updatedAtMillis = 20L,
        )
}
