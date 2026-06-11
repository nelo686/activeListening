package com.mrmustard.activelistening.domain.session

import com.mrmustard.activelistening.domain.structure.SongStructureMap

data class DeletedSavedSong(
    val session: SavedListeningSession,
    val structure: SongStructureMap?,
)

interface SavedSongRepository {
    suspend fun deleteSavedSong(songKey: String): DeletedSavedSong?

    suspend fun restoreSavedSong(deletedSong: DeletedSavedSong)
}
