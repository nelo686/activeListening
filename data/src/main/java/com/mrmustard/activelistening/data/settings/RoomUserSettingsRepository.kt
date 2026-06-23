package com.mrmustard.activelistening.data.settings

import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.settings.UserSettings
import com.mrmustard.activelistening.domain.settings.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomUserSettingsRepository @Inject constructor(
    private val dao: UserSettingsDao,
) : UserSettingsRepository {

    override val settings: Flow<UserSettings> =
        dao.observeSettings().map { entity ->
            entity?.toDomain() ?: UserSettings()
        }

    override suspend fun updateLearningLevel(level: LearningLevel) {
        val current = dao.getSettings() ?: UserSettings().toEntity()
        dao.upsert(current.copy(learningLevel = level.name))
    }

    override suspend fun updateGuidanceIntensity(intensity: GuidanceIntensity) {
        val current = dao.getSettings() ?: UserSettings().toEntity()
        dao.upsert(current.copy(guidanceIntensity = intensity.name))
    }

    private fun UserSettingsEntity.toDomain(): UserSettings =
        UserSettings(
            learningLevel = enumValueOrDefault(learningLevel, LearningLevel.Introductory),
            guidanceIntensity = enumValueOrDefault(guidanceIntensity, GuidanceIntensity.Normal),
        )

    private fun UserSettings.toEntity(): UserSettingsEntity =
        UserSettingsEntity(
            learningLevel = learningLevel.name,
            guidanceIntensity = guidanceIntensity.name,
        )

    private inline fun <reified T : Enum<T>> enumValueOrDefault(
        value: String,
        default: T,
    ): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default
}
