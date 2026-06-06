package com.mrmustard.activelistening.ui.song.guide

data class GuidedListeningMarker(
    val id: Int,
    val positionMillis: Long,
    val title: String,
    val prompt: String,
    val status: GuidedListeningMarkerStatus = GuidedListeningMarkerStatus.Pending,
)

enum class GuidedListeningMarkerStatus {
    Pending,
    Current,
    Reviewed,
    Uncertain,
    Skipped,
}

