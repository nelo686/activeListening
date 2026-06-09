package com.mrmustard.activelistening.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrmustard.activelistening.data.settings.UserSettingsDao
import com.mrmustard.activelistening.data.settings.UserSettingsEntity
import com.mrmustard.activelistening.data.structure.SongStructureDao
import com.mrmustard.activelistening.data.structure.SongStructureSectionEntity

@Database(
    entities = [
        UserSettingsEntity::class,
        SongStructureSectionEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class ActiveListeningDatabase : RoomDatabase() {
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun songStructureDao(): SongStructureDao
}
