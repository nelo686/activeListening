package com.mrmustard.activelistening.domain.guidance

import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast

interface GuidedListeningRepository {
    suspend fun createGuidedListeningPlan(
        request: GuidedListeningRequest,
    ): GuidedListeningResult
}

data class GuidedListeningRequest(
    val songTitle: String,
    val durationMillis: Long,
    val markers: List<GuidedListeningMarkerRequest>,
)

data class GuidedListeningMarkerRequest(
    val id: Int,
    val positionMillis: Long,
    val title: String,
    val prompt: String,
)

sealed interface GuidedListeningResult {
    data class Success(
        val markers: List<GuidedListeningMarkerSuggestion>,
    ) : GuidedListeningResult

    data object MissingApiKey : GuidedListeningResult
    data object UnableToGenerate : GuidedListeningResult
}

data class GuidedListeningMarkerSuggestion(
    val id: Int,
    val title: String,
    val prompt: String,
    val musicalContrast: SectionMusicalContrast? = null,
)
