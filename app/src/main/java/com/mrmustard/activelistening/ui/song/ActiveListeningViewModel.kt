package com.mrmustard.activelistening.ui.song

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.importsong.SongImportResult
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.playback.AudioPlayer
import com.mrmustard.activelistening.domain.settings.UserSettingsRepository
import com.mrmustard.activelistening.domain.structure.SectionBoundary
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import com.mrmustard.activelistening.domain.usecase.GuidedSessionUseCase
import com.mrmustard.activelistening.domain.usecase.ImportSongUseCase
import com.mrmustard.activelistening.domain.usecase.SectionEditingUseCase
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
    private val guidedSessionUseCase: GuidedSessionUseCase,
    private val sectionEditingUseCase: SectionEditingUseCase,
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
        val plan = guidedSessionUseCase(
            playbackState = _uiState.value.playbackState,
            songTitle = _uiState.value.importedSong?.displayName,
            importedSongDurationMillis = _uiState.value.importedSong?.durationMillis,
        )
        _uiState.update { state ->
            state.copy(
                isGuidedSessionActive = true,
                isGuidanceLoading = true,
                guidanceError = null,
                sections = plan.sections,
                selectedSectionId = plan.selectedSectionId,
                activeSectionId = plan.activeSectionId,
                editingSectionId = null,
            ).withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
        }
        audioPlayer.play()

        if (plan.guidanceRequest != null) {
            loadAiGuidance(plan.guidanceRequest)
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
            val validSectionId = sectionEditingUseCase.selectSection(
                sections = state.sections,
                sectionId = sectionId,
            )
            state.copy(selectedSectionId = validSectionId)
                .withEditingSectionLearningContent()
        }
    }

    fun openSectionEditor(sectionId: Int) {
        _uiState.update { state ->
            val selection = sectionEditingUseCase.openEditor(
                sections = state.sections,
                sectionId = sectionId,
            ) ?: return@update state
            state.copy(
                selectedSectionId = selection.selectedSectionId,
                editingSectionId = selection.editingSectionId,
            ).withEditingSectionLearningContent()
        }
    }

    fun closeSectionEditor() {
        _uiState.update { state ->
            val selection = sectionEditingUseCase.closeEditor(state.selectedSectionId)
            state.copy(
                selectedSectionId = selection.selectedSectionId,
                editingSectionId = selection.editingSectionId,
            )
                .withEditingSectionLearningContent()
        }
    }

    fun changeSelectedSectionLabel(label: SectionLabel) {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            val result = sectionEditingUseCase.changeLabel(
                sections = state.sections,
                sectionId = sectionId,
                label = label,
                learningLevel = state.learningLevel,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.editingSectionId,
                editingSectionLearningContent = result.learningContent,
            )
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
            val result = sectionEditingUseCase.setBoundary(
                sections = state.sections,
                sectionId = sectionId,
                boundary = boundary,
                positionMillis = positionMillis,
                learningLevel = state.learningLevel,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.editingSectionId,
                editingSectionLearningContent = result.learningContent,
            ).withSectionProgress(state.playbackState.positionMillis)
        }
    }

    private fun loadAiGuidance(
        request: com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest,
    ) {
        viewModelScope.launch {
            val result = guidedListeningRepository.createGuidedListeningPlan(request)

            _uiState.update { currentState ->
                when (result) {
                    is GuidedListeningResult.Success -> currentState.copy(
                        isGuidanceLoading = false,
                        guidanceError = null,
                        sections = guidedSessionUseCase.mergeSuggestions(
                            sections = currentState.sections,
                            result = result,
                        ),
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
        return copy(
            editingSectionLearningContent = sectionEditingUseCase.learningContent(
                sections = sections,
                editingSectionId = editingSectionId,
                learningLevel = learningLevel,
            ),
        )
    }
}
