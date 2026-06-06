package com.mrmustard.activelistening.domain.settings

import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel

data class UserSettings(
    val learningLevel: LearningLevel = LearningLevel.Introductory,
    val guidanceIntensity: GuidanceIntensity = GuidanceIntensity.Normal,
)
