package com.mrmustard.activelistening.data.structure

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "song_structure_sections",
    primaryKeys = ["song_key", "version", "section_id"],
)
data class SongStructureSectionEntity(
    @ColumnInfo(name = "song_key") val songKey: String,
    @ColumnInfo(name = "version") val version: String,
    @ColumnInfo(name = "section_id") val sectionId: Int,
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "start_millis") val startMillis: Long,
    @ColumnInfo(name = "end_millis") val endMillis: Long,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "prompt") val prompt: String,
    @ColumnInfo(name = "is_approximate") val isApproximate: Boolean,
    @ColumnInfo(name = "musical_contrast_confidence") val musicalContrastConfidence: String?,
    @ColumnInfo(name = "musical_contrast_explanation") val musicalContrastExplanation: String?,
)
