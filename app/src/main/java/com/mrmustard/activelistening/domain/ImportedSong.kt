package com.mrmustard.activelistening.domain

import android.net.Uri

data class ImportedSong(
    val uri: Uri,
    val displayName: String,
    val mimeType: String?,
    val durationMillis: Long,
)
