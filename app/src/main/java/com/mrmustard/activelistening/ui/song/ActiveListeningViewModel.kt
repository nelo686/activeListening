package com.mrmustard.activelistening.ui.song

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrmustard.activelistening.data.playback.AudioPlaybackRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningMarkerRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.ImportedSong
import com.mrmustard.activelistening.domain.SongImportResult
import com.mrmustard.activelistening.domain.structure.SectionBoundary
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureEditor
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import com.mrmustard.activelistening.domain.usecase.ImportSongUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ActiveListeningViewModel @Inject constructor(
    private val importSongUseCase: ImportSongUseCase,
    private val audioPlaybackRepository: AudioPlaybackRepository,
    private val guidedListeningRepository: GuidedListeningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveListeningUiState())
    val uiState: StateFlow<ActiveListeningUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            audioPlaybackRepository.playbackState.collect { playbackState ->
                _uiState.update { state ->
                    state.copy(playbackState = playbackState)
                        .withSectionProgress(playbackState.positionMillis)
                }
            }
        }
    }

    fun importSong(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isImporting = true,
                    importError = null,
                )
            }

            when (val result = importSongUseCase(uri)) {
                is SongImportResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importedSong = result.song,
                            importError = null,
                            isGuidedSessionActive = false,
                            isGuidanceLoading = false,
                            guidanceError = null,
                            sections = emptyList(),
                            selectedSectionId = null,
                            activeSectionId = null,
                            isGuidanceReduced = false,
                        )
                    }
                }

                is SongImportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importError = result.error,
                        )
                    }
                }
            }
        }
    }

    fun play() {
        audioPlaybackRepository.play()
    }

    fun startGuidedSession() {
        val song = _uiState.value.importedSong
        _uiState.update { state ->
            val durationMillis = state.playbackState.durationMillis
                .takeIf { it > 0L }
                ?: state.importedSong?.durationMillis
                ?: 0L

            val sections = SongStructureFactory.createInitialSections(durationMillis)
            val activeSectionId = SongStructureFactory.activeSectionId(
                sections = sections,
                positionMillis = state.playbackState.positionMillis,
            )
            state.copy(
                isGuidedSessionActive = true,
                isGuidanceLoading = true,
                guidanceError = null,
                sections = sections,
                selectedSectionId = activeSectionId ?: sections.firstOrNull()?.id,
                activeSectionId = activeSectionId,
            ).withSectionProgress(state.playbackState.positionMillis)
        }
        audioPlaybackRepository.play()

        if (song != null) {
            loadAiGuidance(song)
        } else {
            _uiState.update {
                it.copy(
                    isGuidanceLoading = false,
                    guidanceError = GuidanceError.UnableToGenerate,
                )
            }
        }
    }

    fun pause() {
        audioPlaybackRepository.pause()
    }

    fun seekTo(positionMillis: Long) {
        audioPlaybackRepository.seekTo(positionMillis)
    }

    fun selectSection(sectionId: Int) {
        _uiState.update { state ->
            state.copy(selectedSectionId = sectionId.takeIf { id -> state.sections.any { it.id == id } })
        }
    }

    fun changeSelectedSectionLabel(label: SectionLabel) {
        _uiState.update { state ->
            val selectedSectionId = state.selectedSectionId ?: return@update state
            state.copy(
                sections = SongStructureEditor.changeLabel(
                    sections = state.sections,
                    sectionId = selectedSectionId,
                    label = label,
                ),
            )
        }
    }

    fun confirmSelectedSection() {
        updateSelectedSectionStatus(SectionStatus.Confirmed)
    }

    fun markSelectedSectionUncertain() {
        updateSelectedSectionStatus(SectionStatus.Uncertain)
    }

    fun adjustSelectedSectionStart(deltaMillis: Long) {
        adjustSelectedSectionBoundary(SectionBoundary.Start, deltaMillis)
    }

    fun adjustSelectedSectionEnd(deltaMillis: Long) {
        adjustSelectedSectionBoundary(SectionBoundary.End, deltaMillis)
    }

    fun toggleGuidanceReduced() {
        _uiState.update { it.copy(isGuidanceReduced = !it.isGuidanceReduced) }
    }

    fun repeatGuidedMarker() {
        val state = _uiState.value
        val section = state.sections.firstOrNull { it.id == state.selectedSectionId }
            ?: state.sections.firstOrNull { it.id == state.activeSectionId }
            ?: return
        audioPlaybackRepository.seekTo(section.startMillis)
        audioPlaybackRepository.play()
    }

    fun clearError() {
        _uiState.update { it.copy(importError = null) }
    }

    override fun onCleared() {
        audioPlaybackRepository.release()
        super.onCleared()
    }

    private fun updateSelectedSectionStatus(status: SectionStatus) {
        _uiState.update { state ->
            val selectedSectionId = state.selectedSectionId ?: return@update state
            state.copy(
                sections = SongStructureEditor.changeStatus(
                    sections = state.sections,
                    sectionId = selectedSectionId,
                    status = status,
                ),
            )
        }
    }

    private fun adjustSelectedSectionBoundary(
        boundary: SectionBoundary,
        deltaMillis: Long,
    ) {
        _uiState.update { state ->
            val selectedSectionId = state.selectedSectionId ?: return@update state
            state.copy(
                sections = SongStructureFactory.adjustSectionBoundary(
                    sections = state.sections,
                    sectionId = selectedSectionId,
                    boundary = boundary,
                    deltaMillis = deltaMillis,
                ),
            ).withSectionProgress(state.playbackState.positionMillis)
        }
    }

    private fun loadAiGuidance(song: ImportedSong) {
        viewModelScope.launch {
            val state = _uiState.value
            val result = guidedListeningRepository.createGuidedListeningPlan(
                GuidedListeningRequest(
                    songTitle = song.displayName,
                    durationMillis = state.playbackState.durationMillis
                        .takeIf { it > 0L }
                        ?: song.durationMillis,
                    markers = state.sections.map { section ->
                        GuidedListeningMarkerRequest(
                            id = section.id,
                            positionMillis = section.startMillis,
                            title = section.label.name,
                            prompt = section.prompt,
                        )
                    },
                ),
            )

            _uiState.update { currentState ->
                when (result) {
                    is GuidedListeningResult.Success -> {
                        currentState.copy(
                            isGuidanceLoading = false,
                            guidanceError = null,
                            sections = currentState.sections.merge(result),
                        ).withSectionProgress(currentState.playbackState.positionMillis)
                    }

                    GuidedListeningResult.MissingApiKey -> currentState.copy(
                        isGuidanceLoading = false,
                        guidanceError = GuidanceError.MissingApiKey,
                    )

                    GuidedListeningResult.UnableToGenerate -> currentState.copy(
                        isGuidanceLoading = false,
                        guidanceError = GuidanceError.UnableToGenerate,
                    )
                }
            }
        }
    }

    private fun List<SongSection>.merge(
        result: GuidedListeningResult.Success,
    ): List<SongSection> {
        val suggestions = result.markers.associateBy { it.id }
        return map { section ->
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

    private fun ActiveListeningUiState.withSectionProgress(positionMillis: Long): ActiveListeningUiState {
        if (!isGuidedSessionActive || sections.isEmpty()) return this

        val activeSectionId = SongStructureFactory.activeSectionId(
            sections = sections,
            positionMillis = positionMillis,
        )
        return copy(
            activeSectionId = activeSectionId,
            selectedSectionId = selectedSectionId ?: activeSectionId,
        )
    }
}
