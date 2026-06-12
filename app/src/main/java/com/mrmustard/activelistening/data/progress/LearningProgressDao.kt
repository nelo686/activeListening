package com.mrmustard.activelistening.data.progress

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningProgressDao {
    @Query("SELECT * FROM learning_progress_sessions ORDER BY started_at_millis ASC")
    fun observeAll(): Flow<List<LearningProgressSessionEntity>>

    @Query("SELECT * FROM learning_progress_sessions WHERE song_key = :songKey ORDER BY started_at_millis ASC")
    suspend fun getForSong(songKey: String): List<LearningProgressSessionEntity>

    @Query("SELECT * FROM learning_progress_sessions WHERE id = :sessionId")
    suspend fun getById(sessionId: Long): LearningProgressSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LearningProgressSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<LearningProgressSessionEntity>)

    @Query("DELETE FROM learning_progress_sessions WHERE song_key = :songKey")
    suspend fun deleteForSong(songKey: String)
}
