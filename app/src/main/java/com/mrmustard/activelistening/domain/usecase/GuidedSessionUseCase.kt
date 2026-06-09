package com.mrmustard.activelistening.domain.usecase

import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.guidance.GuidedListeningMarkerRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import javax.inject.Inject

class GuidedSessionUseCase @Inject constructor() {

    operator fun invoke(
        playbackState: PlaybackState,
        songTitle: String?,
        importedSongDurationMillis: Long?,
    ): GuidedSessionPlan {
        val durationMillis = playbackState.durationMillis
            .takeIf { it > 0L }
            ?: importedSongDurationMillis
            ?: 0L
        val sections = SongStructureFactory.createInitialSections(durationMillis)
        val activeSectionId = SongStructureFactory.activeSectionId(
            sections = sections,
            positionMillis = playbackState.positionMillis,
        )

        return GuidedSessionPlan(
            sections = sections,
            activeSectionId = activeSectionId,
            selectedSectionId = activeSectionId ?: sections.firstOrNull()?.id,
            guidanceRequest = songTitle?.let {
                GuidedListeningRequest(
                    songTitle = songTitle,
                    durationMillis = durationMillis.takeIf { it > 0L } ?: importedSongDurationMillis ?: 0L,
                    markers = sections.map { section ->
                        GuidedListeningMarkerRequest(
                            id = section.id,
                            positionMillis = section.startMillis,
                            title = section.label.name,
                            prompt = section.prompt,
                        )
                    },
                )
            },
        )
    }

    fun mergeSuggestions(
        sections: List<SongSection>,
        result: GuidedListeningResult.Success,
    ): List<SongSection> {
        val suggestions = result.markers.associateBy { it.id }
        return sections.map { section ->
            val suggestion = suggestions[section.id] ?: return@map section
            section.copy(
                label = suggestion.title.toSectionLabel() ?: section.label,
                prompt = suggestion.prompt,
            )
        }
    }

    private fun String.toSectionLabel(): SectionLabel? {
        val normalized = lowercase()
        return when {
            "intro" in normalized || "inicio" in normalized -> SectionLabel.Intro
            "verso" in normalized || "verse" in normalized -> SectionLabel.Verse
            "coro" in normalized || "chorus" in normalized || "estribillo" in normalized -> SectionLabel.Chorus
            "puente" in normalized || "bridge" in normalized -> SectionLabel.Bridge
            "outro" in normalized || "cierre" in normalized || "final" in normalized -> SectionLabel.Outro
            "otra" in normalized || "other" in normalized -> SectionLabel.Other
            else -> null
        }
    }
}

data class GuidedSessionPlan(
    val sections: List<SongSection>,
    val activeSectionId: Int?,
    val selectedSectionId: Int?,
    val guidanceRequest: GuidedListeningRequest?,
)
