package com.mrmustard.activelistening.data.session

import androidx.room.withTransaction
import com.mrmustard.activelistening.data.local.ActiveListeningDatabase
import com.mrmustard.activelistening.domain.session.DeletedSavedSong
import com.mrmustard.activelistening.domain.session.SavedListeningSessionRepository
import com.mrmustard.activelistening.domain.session.SavedSongRepository
import com.mrmustard.activelistening.domain.structure.SongStructureRepository
import javax.inject.Inject

class RoomSavedSongRepository @Inject constructor(
    private val database: ActiveListeningDatabase,
    private val sessionRepository: SavedListeningSessionRepository,
    private val structureRepository: SongStructureRepository,
) : SavedSongRepository {

    override suspend fun deleteSavedSong(songKey: String): DeletedSavedSong? =
        database.withTransaction {
            val session = sessionRepository.getSession(songKey) ?: return@withTransaction null
            val structure = structureRepository.getStructure(songKey)
            sessionRepository.deleteSession(songKey)
            structureRepository.deleteStructure(songKey)
            DeletedSavedSong(session = session, structure = structure)
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
        }
    }
}
