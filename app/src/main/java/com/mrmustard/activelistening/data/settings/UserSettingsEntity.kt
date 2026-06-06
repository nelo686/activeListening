package com.mrmustard.activelistening.data.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = DEFAULT_ID,
    @ColumnInfo(name = "learning_level") val learningLevel: String,
    @ColumnInfo(name = "guidance_intensity") val guidanceIntensity: String,
) {
    companion object {
        const val DEFAULT_ID = 1
    }
}
