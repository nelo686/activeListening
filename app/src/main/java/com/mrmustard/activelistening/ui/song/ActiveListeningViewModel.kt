package com.mrmustard.activelistening.ui.song

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.export.SongMapExportFactory
import com.mrmustard.activelistening.domain.export.SongMapExportRepository
import com.mrmustard.activelistening.domain.export.SongMapExportResult
import com.mrmustard.activelistening.domain.export.SongMapExportValidation
import com.mrmustard.activelistening.domain.export.SongMapExportValidator
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.importsong.SongImportResult
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.playback.AudioPlayer
import com.mrmustard.activelistening.domain.progress.LearningProgressRepository
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.session.SavedListeningSessionRepository
import com.mrmustard.activelistening.domain.session.DeletedSavedSong
import com.mrmustard.activelistening.domain.session.SavedSongRepository
import com.mrmustard.activelistening.domain.settings.UserSettingsRepository
import com.mrmustard.activelistening.domain.structure.SectionBoundary
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import com.mrmustard.activelistening.domain.structure.SongStructureRepository
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
import kotlin.math.abs
import androidx.core.net.toUri

@HiltViewModel
class ActiveListeningViewModel @Inject constructor(
    private val importSongUseCase: ImportSongUseCase,
    private val audioPlayer: AudioPlayer,
    private val guidedListeningRepository: GuidedListeningRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val songStructureRepository: SongStructureRepository,
    private val savedListeningSessionRepository: SavedListeningSessionRepository,
    private val savedSongRepository: SavedSongRepository,
    private val songMapExportRepository: SongMapExportRepository,
    private val guidedSessionUseCase: GuidedSessionUseCase,
    private val sectionEditingUseCase: SectionEditingUseCase,
    private val learningProgressRepository: LearningProgressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveListeningUiState())
    val uiState: StateFlow<ActiveListeningUiState> = _uiState.asStateFlow()
    private var lastPersistedSongKey: String? = null
    private var lastPersistedPositionMillis: Long = 0L
    private var isCurrentSessionSaved = false
    private var lastDeletedSavedSong: DeletedSavedSong? = null
    private var savedSessionDeletionEventId = 0L
    private var progressSessionId: Long? = null

    init {
        observeSavedSessions()
        observePlaybackState()
        observeUserSettings()
        observeProgressSummaries()
    }

    fun importSong(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = null) }

            when (val result = importSongUseCase(uri)) {
                is SongImportResult.Success -> {
                    loadImportedSong(result.song)
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

    fun resumeSavedSession(session: SavedListeningSession) {
        importSong(session.songKey.toUri())
    }

    fun deleteSavedSession(songKey: String) {
        viewModelScope.launch {
            runCatching { savedSongRepository.deleteSavedSong(songKey) }
                .onSuccess { deletedSong ->
                    if (deletedSong == null) {
                        publishSavedSessionDeletionError()
                    } else {
                        lastDeletedSavedSong = deletedSong
                        _uiState.update {
                            it.copy(
                                savedSessionDeletionEvent = SavedSessionDeletionEvent(
                                    id = nextSavedSessionDeletionEventId(),
                                    deletedDisplayName = deletedSong.session.displayName,
                                ),
                            )
                        }
                    }
                }
                .onFailure { publishSavedSessionDeletionError() }
        }
    }

    fun undoSavedSessionDeletion() {
        val deletedSong = lastDeletedSavedSong ?: return
        viewModelScope.launch {
            runCatching { savedSongRepository.restoreSavedSong(deletedSong) }
                .onSuccess {
                    if (lastDeletedSavedSong == deletedSong) {
                        lastDeletedSavedSong = null
                    }
                }
                .onFailure { publishSavedSessionDeletionError() }
        }
    }

    fun clearSavedSessionDeletionEvent() {
        _uiState.update { it.copy(savedSessionDeletionEvent = null) }
    }

    fun returnToStart() {
        val state = _uiState.value
        val songKey = state.importedSong?.uri?.toString()
        val positionMillis = state.playbackState.positionMillis
        audioPlayer.pause()
        if (songKey != null && isCurrentSessionSaved) {
            lastPersistedSongKey = songKey
            lastPersistedPositionMillis = positionMillis
            viewModelScope.launch {
                savedListeningSessionRepository.updatePlaybackPosition(
                    songKey = songKey,
                    positionMillis = positionMillis,
                )
            }
        }
        isCurrentSessionSaved = false
        progressSessionId = null
        _uiState.update {
            it.copy(
                importedSong = null,
                importError = null,
                isGuidedSessionActive = false,
                isGuidanceLoading = false,
                guidanceError = null,
                sections = emptyList(),
                originalSections = emptyList(),
                selectedSectionId = null,
                activeSectionId = null,
                editingSectionId = null,
                editingSectionLearningContent = null,
                isExportingMap = false,
                mapExportError = null,
                exportedMapFileName = null,
            )
        }
    }

    fun play() {
        backToTheBeginning()
        audioPlayer.play()
    }

    private fun backToTheBeginning() {
        val playbackState = _uiState.value.playbackState
        if (playbackState.durationMillis > 0L &&
            playbackState.positionMillis >= playbackState.durationMillis
        ) {
            audioPlayer.seekTo(0L)
        }
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun seekTo(positionMillis: Long) {
        audioPlayer.seekTo(positionMillis)
    }

    fun startGuidedSession() {
        saveCurrentSession()
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
                originalSections = plan.sections,
                selectedSectionId = plan.selectedSectionId,
                activeSectionId = plan.activeSectionId,
                editingSectionId = null,
            ).withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
        }
        audioPlayer.play()
        saveStructure(
            originalSections = plan.sections,
            editedSections = plan.sections,
        )
        _uiState.value.importedSong?.uri?.toString()?.let { songKey ->
            viewModelScope.launch {
                progressSessionId = learningProgressRepository.startSession(
                    songKey = songKey,
                    guidanceIntensity = _uiState.value.guidanceIntensity,
                    totalSections = plan.sections.size,
                )
            }
        }

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
        if (_uiState.value.editingSectionLearningContent != null) {
            recordProgress { learningProgressRepository.recordExplanationConsulted(it) }
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
            ).also(::saveStructure)
        }
        recordManualEdit()
    }

    fun changeSelectedSectionCustomLabel(customLabel: String) {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            val result = sectionEditingUseCase.changeCustomLabel(
                sections = state.sections,
                sectionId = sectionId,
                customLabel = customLabel,
                learningLevel = state.learningLevel,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.editingSectionId,
                editingSectionLearningContent = result.learningContent,
            ).also(::saveStructure)
        }
        recordManualEdit()
    }

    fun cycleSelectedSectionStatus() {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            val result = sectionEditingUseCase.cycleStatus(
                sections = state.sections,
                sectionId = sectionId,
                learningLevel = state.learningLevel,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.editingSectionId,
                editingSectionLearningContent = result.learningContent,
            ).also(::saveStructure)
        }
        val section = _uiState.value.sections.firstOrNull {
            it.id == _uiState.value.currentEditableSectionId()
        }
        if (section?.status == SectionStatus.Confirmed || section?.status == SectionStatus.Uncertain) {
            recordReviewedSection(section.id)
        }
    }

    fun confirmGuidedSection() = setGuidedSectionStatus(SectionStatus.Confirmed)

    fun markGuidedSectionUncertain() = setGuidedSectionStatus(SectionStatus.Uncertain)

    fun repeatGuidedPrompt() {
        audioPlayer.seekTo(
            (_uiState.value.playbackState.positionMillis - GUIDED_REPEAT_MILLIS).coerceAtLeast(0L),
        )
        audioPlayer.play()
        recordProgress { learningProgressRepository.recordRepetition(it) }
    }

    fun skipGuidedSection() {
        val state = _uiState.value
        val currentId = state.activeSectionId ?: state.selectedSectionId ?: return
        val currentIndex = state.sections.indexOfFirst { it.id == currentId }
        val nextSection = state.sections.getOrNull(currentIndex + 1) ?: return
        audioPlayer.seekTo(nextSection.startMillis)
        _uiState.update { it.copy(selectedSectionId = nextSection.id, activeSectionId = nextSection.id) }
    }

    fun repeatSelectedSection() {
        val state = _uiState.value
        val sectionId = state.currentEditableSectionId() ?: return
        val section = state.sections.firstOrNull { it.id == sectionId } ?: return
        audioPlayer.seekTo(section.startMillis)
        audioPlayer.play()
        recordProgress { learningProgressRepository.recordRepetition(it) }
    }

    fun toggleSelectedSectionMusicalContrast() {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            val result = sectionEditingUseCase.toggleMusicalContrast(
                sections = state.sections,
                sectionId = sectionId,
                learningLevel = state.learningLevel,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.editingSectionId,
                editingSectionLearningContent = result.learningContent,
            ).also(::saveStructure)
        }
        recordManualEdit()
    }

    fun setSelectedSectionStart(positionMillis: Long) {
        setSelectedSectionBoundary(SectionBoundary.Start, positionMillis)
    }

    fun setSelectedSectionEnd(positionMillis: Long) {
        setSelectedSectionBoundary(SectionBoundary.End, positionMillis)
    }

    fun splitAtCurrentPosition() {
        _uiState.update { state ->
            val result = sectionEditingUseCase.splitAtPosition(
                sections = state.sections,
                positionMillis = state.playbackState.positionMillis,
                learningLevel = state.learningLevel,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.editingSectionId,
                editingSectionLearningContent = result.learningContent,
            ).withSectionProgress(state.playbackState.positionMillis)
                .also(::saveStructure)
        }
        recordManualEdit()
    }

    fun mergeSelectedSectionWithPrevious() {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            val previousSectionId = state.sections
                .zipWithNext()
                .firstOrNull { (_, right) -> right.id == sectionId }
                ?.first
                ?.id
                ?: return@update state
            val result = sectionEditingUseCase.removeBoundaryAfter(
                sections = state.sections,
                sectionId = previousSectionId,
                learningLevel = state.learningLevel,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.editingSectionId,
                editingSectionLearningContent = result.learningContent,
            ).withSectionProgress(state.playbackState.positionMillis)
                .also(::saveStructure)
        }
        recordManualEdit()
    }

    fun mergeSelectedSectionWithNext() {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            val result = sectionEditingUseCase.removeBoundaryAfter(
                sections = state.sections,
                sectionId = sectionId,
                learningLevel = state.learningLevel,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.editingSectionId,
                editingSectionLearningContent = result.learningContent,
            ).withSectionProgress(state.playbackState.positionMillis)
                .also(::saveStructure)
        }
        recordManualEdit()
    }

    fun restoreOriginalProposal() {
        _uiState.update { state ->
            if (state.originalSections.isEmpty()) return@update state
            val currentSectionId = state.currentEditableSectionId()
            val selectedSectionId = currentSectionId
                ?.takeIf { sectionId ->
                    state.originalSections.any { section -> section.id == sectionId }
                }
                ?: state.originalSections.firstOrNull()?.id
            state.copy(
                sections = state.originalSections,
                selectedSectionId = selectedSectionId,
                editingSectionId = selectedSectionId,
            )
                .withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
                .also(::saveStructure)
        }
    }

    fun exportMap(destination: Uri) {
        viewModelScope.launch {
            val state = _uiState.value
            val song = state.importedSong
            when (SongMapExportValidator.validate(song, state.sections)) {
                SongMapExportValidation.InsufficientStructure -> {
                    _uiState.update {
                        it.copy(
                            isExportingMap = false,
                            mapExportError = MapExportError.InsufficientStructure,
                            exportedMapFileName = null,
                        )
                    }
                    return@launch
                }

                SongMapExportValidation.Valid -> Unit
            }

            val map = SongMapExportFactory.create(
                song = requireNotNull(song),
                sections = state.sections,
                learningLevel = state.learningLevel,
            )
            _uiState.update {
                it.copy(
                    isExportingMap = true,
                    mapExportError = null,
                    exportedMapFileName = null,
                )
            }
            when (songMapExportRepository.exportPdf(destination, map)) {
                SongMapExportResult.Success -> {
                    recordProgress { learningProgressRepository.recordExport(it) }
                    _uiState.update {
                        it.copy(
                            isExportingMap = false,
                            mapExportError = null,
                            exportedMapFileName = map.song.displayName,
                        )
                    }
                }

                SongMapExportResult.UnableToWrite -> _uiState.update {
                    it.copy(
                        isExportingMap = false,
                        mapExportError = MapExportError.UnableToWrite,
                        exportedMapFileName = null,
                    )
                }
            }
        }
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

    fun clearMapExportMessage() {
        _uiState.update {
            it.copy(
                mapExportError = null,
                exportedMapFileName = null,
            )
        }
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
                persistPlaybackPositionIfNeeded(playbackState)
            }
        }
    }

    private fun observeSavedSessions() {
        viewModelScope.launch {
            savedListeningSessionRepository.sessions.collect { sessions ->
                _uiState.update { it.copy(savedSessions = sessions) }
            }
        }
    }

    private fun observeProgressSummaries() {
        viewModelScope.launch {
            learningProgressRepository.summaries.collect { summaries ->
                _uiState.update { it.copy(progressSummaries = summaries) }
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

    private fun publishSavedSessionDeletionError() {
        _uiState.update {
            it.copy(
                savedSessionDeletionEvent = SavedSessionDeletionEvent(
                    id = nextSavedSessionDeletionEventId(),
                ),
            )
        }
    }

    private fun nextSavedSessionDeletionEventId(): Long = ++savedSessionDeletionEventId

    private fun setGuidedSectionStatus(status: SectionStatus) {
        _uiState.update { state ->
            val sectionId = state.activeSectionId ?: state.selectedSectionId ?: return@update state
            val result = sectionEditingUseCase.changeStatus(
                sections = state.sections,
                sectionId = sectionId,
                status = status,
                learningLevel = state.learningLevel,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionLearningContent = result.learningContent,
            ).also(::saveStructure)
        }
        val sectionId = _uiState.value.activeSectionId ?: _uiState.value.selectedSectionId
        if (sectionId != null) recordReviewedSection(sectionId)
    }

    private fun recordManualEdit() {
        recordProgress { learningProgressRepository.recordManualEdit(it) }
    }

    private fun recordReviewedSection(sectionId: Int) {
        recordProgress { learningProgressRepository.markSectionReviewed(it, sectionId) }
    }

    private fun recordProgress(action: suspend (Long) -> Unit) {
        val sessionId = progressSessionId ?: return
        viewModelScope.launch { action(sessionId) }
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
                .also(::saveStructure)
        }
        recordManualEdit()
    }

    private suspend fun loadImportedSong(song: ImportedSong) {
        val songKey = song.uri.toString()
        val savedStructure = songStructureRepository.getStructure(songKey)
        val savedSession = savedListeningSessionRepository.getSession(songKey)
        isCurrentSessionSaved = savedSession != null || savedStructure != null
        if (isCurrentSessionSaved) {
            savedListeningSessionRepository.upsertSession(song)
        }
        lastPersistedSongKey = songKey
        lastPersistedPositionMillis = savedSession?.lastPositionMillis ?: 0L

        _uiState.update {
            val restoredSections = savedStructure?.editedSections.orEmpty()
            it.copy(
                isImporting = false,
                importedSong = song,
                importError = null,
                isGuidedSessionActive = savedStructure != null,
                isGuidanceLoading = false,
                guidanceError = null,
                sections = restoredSections,
                originalSections = savedStructure?.originalSections.orEmpty(),
                selectedSectionId = restoredSections.firstOrNull()?.id,
                activeSectionId = null,
                editingSectionId = null,
                editingSectionLearningContent = null,
                mapExportError = null,
                exportedMapFileName = null,
            )
                .withSectionProgress(savedSession?.lastPositionMillis ?: it.playbackState.positionMillis)
        }
        progressSessionId = null
        if (savedStructure != null) {
            progressSessionId = learningProgressRepository.startSession(
                songKey = songKey,
                guidanceIntensity = _uiState.value.guidanceIntensity,
                totalSections = savedStructure.editedSections.size,
            )
        }
        savedSession?.lastPositionMillis
            ?.takeIf { it > 0L }
            ?.let(audioPlayer::seekTo)
    }

    private fun loadAiGuidance(
        request: com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest,
    ) {
        viewModelScope.launch {
            val result = guidedListeningRepository.createGuidedListeningPlan(request)

            _uiState.update { currentState ->
                when (result) {
                    is GuidedListeningResult.Success -> {
                        val mergedSections = guidedSessionUseCase.mergeSuggestions(
                            sections = currentState.sections,
                            result = result,
                        )
                        val shouldUpdateOriginal = currentState.sections == currentState.originalSections
                        currentState.copy(
                            isGuidanceLoading = false,
                            guidanceError = null,
                            sections = mergedSections,
                            originalSections = if (shouldUpdateOriginal) {
                                mergedSections
                            } else {
                                currentState.originalSections
                            },
                        ).withSectionProgress(currentState.playbackState.positionMillis)
                            .withEditingSectionLearningContent()
                            .also(::saveStructure)
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

    private fun ActiveListeningUiState.currentEditableSectionId(): Int? =
        editingSectionId ?: selectedSectionId

    private fun saveStructure(state: ActiveListeningUiState) {
        saveStructure(
            originalSections = state.originalSections,
            editedSections = state.sections,
        )
    }

    private fun saveStructure(
        originalSections: List<SongSection>,
        editedSections: List<SongSection>,
    ) {
        val songKey = _uiState.value.importedSong?.uri?.toString() ?: return
        if (originalSections.isEmpty() || editedSections.isEmpty()) return
        viewModelScope.launch {
            songStructureRepository.saveStructure(
                songKey = songKey,
                originalSections = originalSections,
                editedSections = editedSections,
            )
        }
    }

    private fun persistPlaybackPositionIfNeeded(playbackState: PlaybackState) {
        if (!isCurrentSessionSaved) return
        val songKey = _uiState.value.importedSong?.uri?.toString() ?: return
        val positionMillis = playbackState.positionMillis.coerceAtLeast(0L)
        if (positionMillis == 0L && !playbackState.isReady) return
        val songChanged = lastPersistedSongKey != songKey
        val movedEnough = abs(positionMillis - lastPersistedPositionMillis) >= POSITION_SAVE_INTERVAL_MILLIS
        if (!songChanged && !movedEnough) return

        lastPersistedSongKey = songKey
        lastPersistedPositionMillis = positionMillis
        viewModelScope.launch {
            savedListeningSessionRepository.updatePlaybackPosition(
                songKey = songKey,
                positionMillis = positionMillis,
            )
        }
    }

    private fun saveCurrentSession() {
        val song = _uiState.value.importedSong ?: return
        isCurrentSessionSaved = true
        lastPersistedSongKey = song.uri.toString()
        viewModelScope.launch {
            savedListeningSessionRepository.upsertSession(song)
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

    private companion object {
        const val POSITION_SAVE_INTERVAL_MILLIS = 5_000L
        const val GUIDED_REPEAT_MILLIS = 8_000L
    }
}
