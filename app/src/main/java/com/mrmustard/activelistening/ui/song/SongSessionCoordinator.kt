package com.mrmustard.activelistening.ui.song

import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.session.SavedListeningSessionRepository
import com.mrmustard.activelistening.domain.session.SongArtworkRepository
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureMap
import com.mrmustard.activelistening.domain.structure.SongStructureRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@ViewModelScoped
class SongSessionCoordinator @Inject constructor(
    private val sessionRepository: SavedListeningSessionRepository,
    private val structureRepository: SongStructureRepository,
    private val artworkRepository: SongArtworkRepository,
) {
    val savedSessions: Flow<List<SavedListeningSession>> = sessionRepository.sessions

    private var lastPersistedSongKey: String? = null
    private var lastPersistedPositionMillis = 0L
    private var isCurrentSessionSaved = false

    suspend fun load(song: ImportedSong): LoadedSongSession {
        val songKey = song.uri
        val structure = structureRepository.getStructure(songKey)
        val session = sessionRepository.getSession(songKey)
        isCurrentSessionSaved = session != null || structure != null
        if (isCurrentSessionSaved) sessionRepository.upsertSession(song)
        lastPersistedSongKey = songKey
        lastPersistedPositionMillis = session?.lastPositionMillis ?: 0L
        return LoadedSongSession(structure, session)
    }

    suspend fun loadArtwork(songKey: String): ByteArray? = artworkRepository.load(songKey)

    fun markSaved(scope: CoroutineScope, song: ImportedSong) {
        isCurrentSessionSaved = true
        lastPersistedSongKey = song.uri
        scope.launch { sessionRepository.upsertSession(song) }
    }

    fun leave(scope: CoroutineScope, songKey: String?, positionMillis: Long) {
        if (songKey != null && isCurrentSessionSaved) {
            lastPersistedSongKey = songKey
            lastPersistedPositionMillis = positionMillis
            scope.launch { sessionRepository.updatePlaybackPosition(songKey, positionMillis) }
        }
        isCurrentSessionSaved = false
    }

    fun persistPositionIfNeeded(
        scope: CoroutineScope,
        songKey: String?,
        playbackState: PlaybackState,
    ) {
        if (!isCurrentSessionSaved || songKey == null) return
        val positionMillis = playbackState.positionMillis.coerceAtLeast(0L)
        if (positionMillis == 0L && !playbackState.isReady) return
        val songChanged = lastPersistedSongKey != songKey
        val movedEnough = abs(positionMillis - lastPersistedPositionMillis) >= POSITION_SAVE_INTERVAL_MILLIS
        if (!songChanged && !movedEnough) return
        lastPersistedSongKey = songKey
        lastPersistedPositionMillis = positionMillis
        scope.launch { sessionRepository.updatePlaybackPosition(songKey, positionMillis) }
    }

    fun saveStructure(
        scope: CoroutineScope,
        songKey: String?,
        originalSections: List<SongSection>,
        editedSections: List<SongSection>,
    ) {
        if (songKey == null || originalSections.isEmpty() || editedSections.isEmpty()) return
        scope.launch {
            structureRepository.saveStructure(songKey, originalSections, editedSections)
        }
    }

    private companion object {
        const val POSITION_SAVE_INTERVAL_MILLIS = 5_000L
    }
}

data class LoadedSongSession(
    val structure: SongStructureMap?,
    val session: SavedListeningSession?,
)
