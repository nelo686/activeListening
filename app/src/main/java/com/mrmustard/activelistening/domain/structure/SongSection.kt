package com.mrmustard.activelistening.domain.structure

data class SongSection(
    val id: Int,
    val startMillis: Long,
    val endMillis: Long,
    val label: SectionLabel,
    val customLabel: String? = null,
    val status: SectionStatus = SectionStatus.Suggested,
    val prompt: String,
    val isApproximate: Boolean = true,
    val rhythmInfo: SectionRhythmInfo? = SectionRhythmEstimator.estimate(endMillis - startMillis),
    val musicalContrast: SectionMusicalContrast? = null,
) {
    val durationMillis: Long
        get() = (endMillis - startMillis).coerceAtLeast(0L)
}
