package com.mrmustard.activelistening.di

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.mrmustard.activelistening.BuildConfig
import com.mrmustard.activelistening.data.guidance.OpenAiGuidanceConfig
import com.mrmustard.activelistening.data.guidance.OpenAiGuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
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
                apiKey = BuildConfig.DEVEXPERT_API_KEY,
                model = BuildConfig.DEVEXPERT_GUIDANCE_MODEL,
                baseUrl = BuildConfig.DEVEXPERT_BASE_URL,
            )

        @Provides
        @ViewModelScoped
        fun provideOpenAI(config: OpenAiGuidanceConfig): OpenAI =
            OpenAI(
                token = config.apiKey,
                host = OpenAIHost(baseUrl = config.baseUrl.ensureTrailingSlash()),
            )

        private fun String.ensureTrailingSlash(): String =
            if (endsWith("/")) this else "$this/"
    }
}
