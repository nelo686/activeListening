package com.mrmustard.activelistening.domain.settings

import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun updateLearningLevel(level: LearningLevel)

    suspend fun updateGuidanceIntensity(intensity: GuidanceIntensity)
}
