package com.mrmustard.activelistening.data.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedListeningSessionDao {

    @Query("SELECT * FROM saved_listening_sessions ORDER BY updated_at_millis DESC")
    fun observeSessions(): Flow<List<SavedListeningSessionEntity>>

    @Query("SELECT * FROM saved_listening_sessions WHERE song_key = :songKey")
    suspend fun getSession(songKey: String): SavedListeningSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SavedListeningSessionEntity)

    @Query(
        """
        UPDATE saved_listening_sessions
        SET last_position_millis = :positionMillis,
            updated_at_millis = :updatedAtMillis
        WHERE song_key = :songKey
        """,
    )
    suspend fun updatePlaybackPosition(
        songKey: String,
        positionMillis: Long,
        updatedAtMillis: Long,
    )
}
