package com.mrmustard.activelistening.ui.song

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrmustard.activelistening.domain.guidance.GuidedListeningMarkerRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.importsong.SongImportResult
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.learning.SectionExplanationProvider
import com.mrmustard.activelistening.domain.playback.AudioPlayer
import com.mrmustard.activelistening.domain.settings.UserSettingsRepository
import com.mrmustard.activelistening.domain.structure.SectionBoundary
import com.mrmustard.activelistening.domain.structure.SectionLabel
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
    private val audioPlayer: AudioPlayer,
    private val guidedListeningRepository: GuidedListeningRepository,
    private val userSettingsRepository: UserSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveListeningUiState())
    val uiState: StateFlow<ActiveListeningUiState> = _uiState.asStateFlow()

    init {
        observePlaybackState()
        observeUserSettings()
    }

    fun importSong(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = null) }

            when (val result = importSongUseCase(uri)) {
                is SongImportResult.Success -> _uiState.update {
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
                        editingSectionId = null,
                        editingSectionLearningContent = null,
                    )
                }

                is SongImportResult.Error -> _uiState.update {
                    it.copy(
                        isImporting = false,
                        importError = result.error,
                    )
                }
            }
        }
    }

    fun play() {
        audioPlayer.play()
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun seekTo(positionMillis: Long) {
        audioPlayer.seekTo(positionMillis)
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
                editingSectionId = null,
            ).withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
        }
        audioPlayer.play()

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

    fun selectSection(sectionId: Int) {
        _uiState.update { state ->
            val validSectionId = sectionId.takeIf { id -> state.sections.any { it.id == id } }
            state.copy(selectedSectionId = validSectionId)
                .withEditingSectionLearningContent()
        }
    }

    fun openSectionEditor(sectionId: Int) {
        _uiState.update { state ->
            val validSectionId = sectionId.takeIf { id -> state.sections.any { it.id == id } }
                ?: return@update state
            state.copy(
                selectedSectionId = validSectionId,
                editingSectionId = validSectionId,
            ).withEditingSectionLearningContent()
        }
    }

    fun closeSectionEditor() {
        _uiState.update {
            it.copy(editingSectionId = null)
                .withEditingSectionLearningContent()
        }
    }

    fun changeSelectedSectionLabel(label: SectionLabel) {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            state.copy(
                sections = SongStructureEditor.changeLabel(
                    sections = state.sections,
                    sectionId = sectionId,
                    label = label,
                ),
                selectedSectionId = sectionId,
                editingSectionId = sectionId,
            ).withEditingSectionLearningContent()
        }
    }

    fun setSelectedSectionStart(positionMillis: Long) {
        setSelectedSectionBoundary(SectionBoundary.Start, positionMillis)
    }

    fun setSelectedSectionEnd(positionMillis: Long) {
        setSelectedSectionBoundary(SectionBoundary.End, positionMillis)
    }

    fun changeGuidanceIntensity(intensity: GuidanceIntensity) {
        _uiState.update { it.copy(guidanceIntensity = intensity) }
        viewModelScope.launch {
            userSettingsRepository.updateGuidanceIntensity(intensity)
        }
    }

    fun changeLearningLevel(level: LearningLevel) {
        _uiState.update {
            it.copy(learningLevel = level)
                .withEditingSectionLearningContent()
        }
        viewModelScope.launch {
            userSettingsRepository.updateLearningLevel(level)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(importError = null) }
    }

    override fun onCleared() {
        audioPlayer.release()
        super.onCleared()
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            audioPlayer.playbackState.collect { playbackState ->
                _uiState.update { state ->
                    state.copy(playbackState = playbackState)
                        .withSectionProgress(playbackState.positionMillis)
                        .withEditingSectionLearningContent()
                }
            }
        }
    }

    private fun observeUserSettings() {
        viewModelScope.launch {
            userSettingsRepository.settings.collect { settings ->
                _uiState.update { state ->
                    state.copy(
                        learningLevel = settings.learningLevel,
                        guidanceIntensity = settings.guidanceIntensity,
                    ).withEditingSectionLearningContent()
                }
            }
        }
    }

    private fun setSelectedSectionBoundary(
        boundary: SectionBoundary,
        positionMillis: Long,
    ) {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            state.copy(
                sections = SongStructureFactory.setSectionBoundary(
                    sections = state.sections,
                    sectionId = sectionId,
                    boundary = boundary,
                    positionMillis = positionMillis,
                ),
                selectedSectionId = sectionId,
                editingSectionId = sectionId,
            ).withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
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
                    is GuidedListeningResult.Success -> currentState.copy(
                        isGuidanceLoading = false,
                        guidanceError = null,
                        sections = currentState.sections.merge(result),
                    ).withSectionProgress(currentState.playbackState.positionMillis)
                        .withEditingSectionLearningContent()

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

    private fun ActiveListeningUiState.currentEditableSectionId(): Int? =
        editingSectionId ?: selectedSectionId

    private fun ActiveListeningUiState.withSectionProgress(positionMillis: Long): ActiveListeningUiState {
        if (!isGuidedSessionActive || sections.isEmpty()) return this

        val activeSectionId = SongStructureFactory.activeSectionId(
            sections = sections,
            positionMillis = positionMillis,
        )
        return copy(
            activeSectionId = activeSectionId,
            selectedSectionId = when (selectedSectionId) {
                null, this.activeSectionId -> activeSectionId
                else -> selectedSectionId
            },
        )
    }

    private fun ActiveListeningUiState.withEditingSectionLearningContent(): ActiveListeningUiState {
        val section = sections.firstOrNull { it.id == editingSectionId }
        return copy(
            editingSectionLearningContent = section?.let {
                SectionExplanationProvider.contentFor(
                    label = it.label,
                    level = learningLevel,
                    status = it.status,
                )
            },
        )
    }
}
