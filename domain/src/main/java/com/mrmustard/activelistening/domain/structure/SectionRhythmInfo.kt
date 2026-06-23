package com.mrmustard.activelistening.domain.structure

data class SectionRhythmInfo(
    val estimatedBars: Int?,
    val confidence: SectionRhythmConfidence,
    val regularity: SectionRhythmRegularity,
)

enum class SectionRhythmConfidence {
    Moderate,
    Low,
}

enum class SectionRhythmRegularity {
    Regular,
    Approximate,
    Irregular,
}
