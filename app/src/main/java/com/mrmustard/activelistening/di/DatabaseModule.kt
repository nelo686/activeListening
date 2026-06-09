package com.mrmustard.activelistening.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mrmustard.activelistening.data.local.ActiveListeningDatabase
import com.mrmustard.activelistening.data.settings.RoomUserSettingsRepository
import com.mrmustard.activelistening.data.settings.UserSettingsDao
import com.mrmustard.activelistening.data.structure.RoomSongStructureRepository
import com.mrmustard.activelistening.data.structure.SongStructureDao
import com.mrmustard.activelistening.domain.settings.UserSettingsRepository
import com.mrmustard.activelistening.domain.structure.SongStructureRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(
        repository: RoomUserSettingsRepository,
    ): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindSongStructureRepository(
        repository: RoomSongStructureRepository,
    ): SongStructureRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context,
        ): ActiveListeningDatabase =
            Room.databaseBuilder(
                context,
                ActiveListeningDatabase::class.java,
                "active-listening.db",
            )
                .addMigrations(MIGRATION_1_2)
                .build()

        @Provides
        fun provideUserSettingsDao(database: ActiveListeningDatabase): UserSettingsDao =
            database.userSettingsDao()

        @Provides
        fun provideSongStructureDao(database: ActiveListeningDatabase): SongStructureDao =
            database.songStructureDao()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS song_structure_sections (
                        song_key TEXT NOT NULL,
                        version TEXT NOT NULL,
                        section_id INTEGER NOT NULL,
                        position INTEGER NOT NULL,
                        start_millis INTEGER NOT NULL,
                        end_millis INTEGER NOT NULL,
                        label TEXT NOT NULL,
                        status TEXT NOT NULL,
                        prompt TEXT NOT NULL,
                        is_approximate INTEGER NOT NULL,
                        musical_contrast_confidence TEXT,
                        musical_contrast_explanation TEXT,
                        PRIMARY KEY(song_key, version, section_id)
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
