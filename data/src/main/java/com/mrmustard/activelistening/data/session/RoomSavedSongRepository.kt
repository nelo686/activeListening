package com.mrmustard.activelistening.data.session

import androidx.room.withTransaction
import com.mrmustard.activelistening.data.local.ActiveListeningDatabase
import com.mrmustard.activelistening.domain.session.DeletedSavedSong
import com.mrmustard.activelistening.domain.session.SavedListeningSessionRepository
import com.mrmustard.activelistening.domain.session.SavedSongRepository
import com.mrmustard.activelistening.domain.structure.SongStructureRepository
import com.mrmustard.activelistening.domain.progress.LearningProgressRepository
import javax.inject.Inject

class RoomSavedSongRepository @Inject constructor(
    private val database: ActiveListeningDatabase,
    private val sessionRepository: SavedListeningSessionRepository,
    private val structureRepository: SongStructureRepository,
    private val progressRepository: LearningProgressRepository,
) : SavedSongRepository {

    override suspend fun deleteSavedSong(songKey: String): DeletedSavedSong? =
        database.withTransaction {
            val session = sessionRepository.getSession(songKey) ?: return@withTransaction null
            val structure = structureRepository.getStructure(songKey)
            val progressSessions = progressRepository.getSessions(songKey)
            sessionRepository.deleteSession(songKey)
            structureRepository.deleteStructure(songKey)
            progressRepository.deleteSessions(songKey)
            DeletedSavedSong(session = session, structure = structure, progressSessions = progressSessions)
        }

    override suspend fun restoreSavedSong(deletedSong: DeletedSavedSong) {
        database.withTransaction {
            sessionRepository.restoreSession(deletedSong.session)
            structureRepository.deleteStructure(deletedSong.session.songKey)
            deletedSong.structure?.let { structure ->
                structureRepository.saveStructure(
                    songKey = deletedSong.session.songKey,
                    originalSections = structure.originalSections,
                    editedSections = structure.editedSections,
                )
            }
            progressRepository.replaceSessions(
                songKey = deletedSong.session.songKey,
                sessions = deletedSong.progressSessions,
            )
        }
    }
}
