package com.mrmustard.activelistening.di

import android.content.Context
import androidx.room.Room
import com.mrmustard.activelistening.data.local.ActiveListeningDatabase
import com.mrmustard.activelistening.data.local.DatabaseMigrations
import com.mrmustard.activelistening.data.session.RoomSavedListeningSessionRepository
import com.mrmustard.activelistening.data.session.RoomSavedSongRepository
import com.mrmustard.activelistening.data.session.SavedListeningSessionDao
import com.mrmustard.activelistening.data.settings.RoomUserSettingsRepository
import com.mrmustard.activelistening.data.settings.UserSettingsDao
import com.mrmustard.activelistening.data.structure.RoomSongStructureRepository
import com.mrmustard.activelistening.data.structure.SongStructureDao
import com.mrmustard.activelistening.domain.session.SavedListeningSessionRepository
import com.mrmustard.activelistening.domain.session.SavedSongRepository
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

    @Binds
    @Singleton
    abstract fun bindSavedListeningSessionRepository(
        repository: RoomSavedListeningSessionRepository,
    ): SavedListeningSessionRepository

    @Binds
    @Singleton
    abstract fun bindSavedSongRepository(
        repository: RoomSavedSongRepository,
    ): SavedSongRepository

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
                .addMigrations(*DatabaseMigrations.all)
                .build()

        @Provides
        fun provideUserSettingsDao(database: ActiveListeningDatabase): UserSettingsDao =
            database.userSettingsDao()

        @Provides
        fun provideSongStructureDao(database: ActiveListeningDatabase): SongStructureDao =
            database.songStructureDao()

        @Provides
        fun provideSavedListeningSessionDao(database: ActiveListeningDatabase): SavedListeningSessionDao =
            database.savedListeningSessionDao()
    }
}
