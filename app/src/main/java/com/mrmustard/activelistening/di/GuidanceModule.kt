package com.mrmustard.activelistening.di

import com.aallam.openai.client.OpenAI
import com.mrmustard.activelistening.BuildConfig
import com.mrmustard.activelistening.data.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.data.guidance.OpenAiGuidanceConfig
import com.mrmustard.activelistening.data.guidance.OpenAiGuidedListeningRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class GuidanceModule {

    @Binds
    abstract fun bindGuidedListeningRepository(
        repository: OpenAiGuidedListeningRepository,
    ): GuidedListeningRepository

    companion object {
        @Provides
        @ViewModelScoped
        fun provideOpenAiGuidanceConfig(): OpenAiGuidanceConfig =
            OpenAiGuidanceConfig(
                apiKey = BuildConfig.OPENAI_API_KEY,
                model = BuildConfig.OPENAI_GUIDANCE_MODEL,
            )

        @Provides
        @ViewModelScoped
        fun provideOpenAI(config: OpenAiGuidanceConfig): OpenAI =
            OpenAI(token = config.apiKey)
    }
}

