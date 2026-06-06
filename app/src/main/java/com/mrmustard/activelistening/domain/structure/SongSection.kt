package com.mrmustard.activelistening.domain.structure

data class SongSection(
    val id: Int,
    val startMillis: Long,
    val endMillis: Long,
    val label: SectionLabel,
    val status: SectionStatus = SectionStatus.Suggested,
    val prompt: String,
    val isApproximate: Boolean = true,
) {
    val durationMillis: Long
        get() = (endMillis - startMillis).coerceAtLeast(0L)
}
