package com.mrmustard.activelistening.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrmustard.activelistening.data.settings.UserSettingsDao
import com.mrmustard.activelistening.data.settings.UserSettingsEntity

@Database(
    entities = [UserSettingsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ActiveListeningDatabase : RoomDatabase() {
    abstract fun userSettingsDao(): UserSettingsDao
}
