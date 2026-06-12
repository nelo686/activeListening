package com.mrmustard.activelistening.data.progress

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_progress_sessions")
data class LearningProgressSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "song_key") val songKey: String,
    @ColumnInfo(name = "started_at_millis") val startedAtMillis: Long,
    @ColumnInfo(name = "updated_at_millis") val updatedAtMillis: Long,
    @ColumnInfo(name = "guidance_intensity") val guidanceIntensity: String,
    @ColumnInfo(name = "total_sections") val totalSections: Int,
    @ColumnInfo(name = "reviewed_section_ids") val reviewedSectionIds: String,
    @ColumnInfo(name = "manual_edits") val manualEdits: Int,
    @ColumnInfo(name = "repetitions") val repetitions: Int,
    @ColumnInfo(name = "explanations_consulted") val explanationsConsulted: Int,
    @ColumnInfo(name = "exports") val exports: Int,
)
