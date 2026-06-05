package com.mrmustard.activelistening.di

import com.mrmustard.activelistening.data.importing.AndroidSongImportRepository
import com.mrmustard.activelistening.data.importing.SongImportRepository
import com.mrmustard.activelistening.data.playback.AudioPlaybackRepository
import com.mrmustard.activelistening.data.playback.ExoPlayerAudioPlaybackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindSongImportRepository(
        repository: AndroidSongImportRepository,
    ): SongImportRepository

    @Binds
    abstract fun bindAudioPlaybackRepository(
        repository: ExoPlayerAudioPlaybackRepository,
    ): AudioPlaybackRepository
}
