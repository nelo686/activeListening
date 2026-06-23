package com.mrmustard.activelistening.domain.session

import com.mrmustard.activelistening.domain.structure.SongStructureMap
import com.mrmustard.activelistening.domain.progress.LearningProgressSession

data class DeletedSavedSong(
    val session: SavedListeningSession,
    val structure: SongStructureMap?,
    val progressSessions: List<LearningProgressSession> = emptyList(),
)

interface SavedSongRepository {
    suspend fun deleteSavedSong(songKey: String): DeletedSavedSong?

    suspend fun restoreSavedSong(deletedSong: DeletedSavedSong)
}
