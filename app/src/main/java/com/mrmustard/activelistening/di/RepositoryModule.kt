package com.mrmustard.activelistening.di

import com.mrmustard.activelistening.data.importing.AndroidSongImportGateway
import com.mrmustard.activelistening.data.playback.ExoPlayerAudioPlayer
import com.mrmustard.activelistening.domain.importsong.SongImportGateway
import com.mrmustard.activelistening.domain.playback.AudioPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindSongImportGateway(
        gateway: AndroidSongImportGateway,
    ): SongImportGateway

    @Binds
    abstract fun bindAudioPlayer(
        audioPlayer: ExoPlayerAudioPlayer,
    ): AudioPlayer
}
