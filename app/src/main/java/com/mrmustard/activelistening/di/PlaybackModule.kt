package com.mrmustard.activelistening.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object PlaybackModule {
    @Provides
    @ViewModelScoped
    fun provideExoPlayer(
        @ApplicationContext context: Context,
    ): ExoPlayer = ExoPlayer.Builder(context).build()
}
