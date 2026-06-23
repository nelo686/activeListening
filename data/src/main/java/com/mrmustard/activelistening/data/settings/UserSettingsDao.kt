package com.mrmustard.activelistening.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = :id")
    fun observeSettings(id: Int = UserSettingsEntity.DEFAULT_ID): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = :id")
    suspend fun getSettings(id: Int = UserSettingsEntity.DEFAULT_ID): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: UserSettingsEntity)
}
