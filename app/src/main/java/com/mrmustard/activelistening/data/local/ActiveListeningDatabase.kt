package com.mrmustard.activelistening.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrmustard.activelistening.data.session.SavedListeningSessionDao
import com.mrmustard.activelistening.data.session.SavedListeningSessionEntity
import com.mrmustard.activelistening.data.settings.UserSettingsDao
import com.mrmustard.activelistening.data.settings.UserSettingsEntity
import com.mrmustard.activelistening.data.structure.SongStructureDao
import com.mrmustard.activelistening.data.structure.SongStructureSectionEntity

@Database(
    entities = [
        UserSettingsEntity::class,
        SongStructureSectionEntity::class,
        SavedListeningSessionEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class ActiveListeningDatabase : RoomDatabase() {
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun songStructureDao(): SongStructureDao
    abstract fun savedListeningSessionDao(): SavedListeningSessionDao
}
