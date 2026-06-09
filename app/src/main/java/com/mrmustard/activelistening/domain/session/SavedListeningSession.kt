package com.mrmustard.activelistening.domain.session

data class SavedListeningSession(
    val songKey: String,
    val displayName: String,
    val mimeType: String?,
    val durationMillis: Long,
    val lastPositionMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
