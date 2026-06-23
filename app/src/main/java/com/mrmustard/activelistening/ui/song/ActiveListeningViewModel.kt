package com.mrmustard.activelistening.ui.song

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.importsong.SongImportResult
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.learning.SectionExplanationProvider
import com.mrmustard.activelistening.domain.playback.AudioPlayer
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.session.DeletedSavedSong
import com.mrmustard.activelistening.domain.session.SavedSongRepository
import com.mrmustard.activelistening.domain.settings.UserSettingsRepository
import com.mrmustard.activelistening.domain.structure.SectionBoundary
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import com.mrmustard.activelistening.domain.usecase.ExportSongMapResult
import com.mrmustard.activelistening.domain.usecase.ExportSongMapUseCase
import com.mrmustard.activelistening.domain.usecase.ImportSongUseCase
import com.mrmustard.activelistening.domain.usecase.SectionEditingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@HiltViewModel
class ActiveListeningViewModel @Inject constructor(
    private val importSongUseCase: ImportSongUseCase,
    private val audioPlayer: AudioPlayer,
    private val guidedListeningCoordinator: GuidedListeningCoordinator,
    private val userSettingsRepository: UserSettingsRepository,
    private val songSessionCoordinator: SongSessionCoordinator,
    private val savedSongRepository: SavedSongRepository,
    private val exportSongMapUseCase: ExportSongMapUseCase,
    private val sectionEditingUseCase: SectionEditingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveListeningUiState())
    val uiState: StateFlow<ActiveListeningUiState> = _uiState.asStateFlow()
    private var lastDeletedSavedSong: DeletedSavedSong? = null
    private var savedSessionDeletionEventId = 0L

    init {
        observeSavedSessions()
        observePlaybackState()
        observeUserSettings()
        observeProgressSummaries()
    }

    fun importSong(uri: Uri) {
        guidedListeningCoordinator.cancel()
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = null) }

            when (val result = importSongUseCase(uri.toString())) {
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

    fun loadSavedSongArtwork(songKey: String) {
        if (_uiState.value.savedSongArtwork.containsKey(songKey)) return
        viewModelScope.launch {
            val artwork = songSessionCoordinator.loadArtwork(songKey)
            _uiState.update { state ->
                state.copy(savedSongArtwork = state.savedSongArtwork + (songKey to artwork))
            }
        }
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
        val songKey = state.importedSong?.uri
        val positionMillis = state.playbackState.positionMillis
        audioPlayer.pause()
        songSessionCoordinator.leave(viewModelScope, songKey, positionMillis)
        guidedListeningCoordinator.cancel()
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
        guidedListeningCoordinator.cancel()
        saveCurrentSession()
        val plan = guidedListeningCoordinator.createPlan(
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
        _uiState.value.importedSong?.uri?.let { songKey ->
            guidedListeningCoordinator.startProgressSession(
                scope = viewModelScope,
                songKey = songKey,
                guidanceIntensity = _uiState.value.guidanceIntensity,
                totalSections = plan.sections.size,
            )
        }

        val guidanceRequest = plan.guidanceRequest
        if (guidanceRequest != null) {
            val songKey = _uiState.value.importedSong?.uri
            if (songKey != null) {
                loadAiGuidance(
                    request = guidanceRequest,
                    songKey = songKey,
                )
            }
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
            if (state.sections.none { it.id == sectionId }) return@update state
            state.copy(
                selectedSectionId = sectionId,
                editingSectionId = sectionId,
            ).withEditingSectionLearningContent()
        }
        if (_uiState.value.editingSectionLearningContent != null) {
            guidedListeningCoordinator.recordExplanationConsulted(viewModelScope)
        }
    }

    fun closeSectionEditor() {
        _uiState.update { state ->
            state.copy(editingSectionId = null)
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
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = state.editingSectionId,
            ).withEditingSectionLearningContent()
                .also(::saveStructure)
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
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = state.editingSectionId,
            ).withEditingSectionLearningContent()
                .also(::saveStructure)
        }
        recordManualEdit()
    }

    fun cycleSelectedSectionStatus() {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            val result = sectionEditingUseCase.cycleStatus(
                sections = state.sections,
                sectionId = sectionId,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = state.editingSectionId,
            ).withEditingSectionLearningContent()
                .also(::saveStructure)
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
        guidedListeningCoordinator.recordRepetition(viewModelScope)
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
        guidedListeningCoordinator.recordRepetition(viewModelScope)
    }

    fun toggleSelectedSectionMusicalContrast() {
        _uiState.update { state ->
            val sectionId = state.currentEditableSectionId() ?: return@update state
            val result = sectionEditingUseCase.toggleMusicalContrast(
                sections = state.sections,
                sectionId = sectionId,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = state.editingSectionId,
            ).withEditingSectionLearningContent()
                .also(::saveStructure)
        }
        recordManualEdit()
    }

    fun setSelectedSectionStart(positionMillis: Long) {
        setSelectedSectionBoundary(SectionBoundary.Start, positionMillis)
    }

    fun setSelectedSectionEnd(positionMillis: Long) {
        setSelectedSectionBoundary(SectionBoundary.End, positionMillis)
    }

    fun setTimelineBoundaryAfter(sectionId: Int, positionMillis: Long) {
        _uiState.update { state ->
            val result = sectionEditingUseCase.setBoundary(
                sections = state.sections,
                sectionId = sectionId,
                boundary = SectionBoundary.End,
                positionMillis = positionMillis,
            )
            state.copy(sections = result.sections)
                .withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
                .also(::saveStructure)
        }
        recordManualEdit()
    }

    fun splitAtCurrentPosition() {
        _uiState.update { state ->
            val result = sectionEditingUseCase.splitAtPosition(
                sections = state.sections,
                positionMillis = state.playbackState.positionMillis,
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.selectedSectionId,
            ).withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
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
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.selectedSectionId,
            ).withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
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
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = result.selectedSectionId,
            ).withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
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
            _uiState.update {
                it.copy(
                    isExportingMap = true,
                    mapExportError = null,
                    exportedMapFileName = null,
                )
            }
            when (val result = exportSongMapUseCase(
                destination = destination.toString(),
                song = state.importedSong,
                sections = state.sections,
                learningLevel = state.learningLevel,
            )) {
                is ExportSongMapResult.Success -> {
                    guidedListeningCoordinator.recordExport(viewModelScope)
                    _uiState.update {
                        it.copy(
                            isExportingMap = false,
                            mapExportError = null,
                            exportedMapFileName = result.displayName,
                        )
                    }
                }

                ExportSongMapResult.InsufficientStructure -> _uiState.update {
                    it.copy(
                        isExportingMap = false,
                        mapExportError = MapExportError.InsufficientStructure,
                        exportedMapFileName = null,
                    )
                }

                ExportSongMapResult.UnableToWrite -> _uiState.update {
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
        guidedListeningCoordinator.cancel()
        audioPlayer.release()
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
            songSessionCoordinator.savedSessions.collect { sessions ->
                _uiState.update { it.copy(savedSessions = sessions) }
            }
        }
    }

    private fun observeProgressSummaries() {
        viewModelScope.launch {
            guidedListeningCoordinator.progressSummaries.collect { summaries ->
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
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
            ).withEditingSectionLearningContent()
                .also(::saveStructure)
        }
        val sectionId = _uiState.value.activeSectionId ?: _uiState.value.selectedSectionId
        if (sectionId != null) recordReviewedSection(sectionId)
    }

    private fun recordManualEdit() {
        guidedListeningCoordinator.recordManualEdit(viewModelScope)
    }

    private fun recordReviewedSection(sectionId: Int) {
        guidedListeningCoordinator.recordReviewedSection(viewModelScope, sectionId)
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
            )
            state.copy(
                sections = result.sections,
                selectedSectionId = result.selectedSectionId,
                editingSectionId = state.editingSectionId,
            ).withSectionProgress(state.playbackState.positionMillis)
                .withEditingSectionLearningContent()
                .also(::saveStructure)
        }
        recordManualEdit()
    }

    private suspend fun loadImportedSong(song: ImportedSong) {
        val songKey = song.uri
        val loadedSession = songSessionCoordinator.load(song)
        val savedStructure = loadedSession.structure
        val savedSession = loadedSession.session

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
        if (savedStructure != null) {
            guidedListeningCoordinator.restoreProgressSession(
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
        songKey: String,
    ) {
        guidedListeningCoordinator.requestGuidance(
            scope = viewModelScope,
            request = request,
            songKey = songKey,
            isCurrentSong = {
                val state = _uiState.value
                state.importedSong?.uri == songKey && state.isGuidedSessionActive
            },
        ) { result ->
            _uiState.update { latestState ->
                when (result) {
                    is GuidedListeningResult.Success -> {
                        val mergedSections = guidedListeningCoordinator.mergeSuggestions(
                            sections = latestState.sections,
                            result = result,
                        )
                        val shouldUpdateOriginal = latestState.sections == latestState.originalSections
                        latestState.copy(
                            isGuidanceLoading = false,
                            guidanceError = null,
                            sections = mergedSections,
                            originalSections = if (shouldUpdateOriginal) {
                                mergedSections
                            } else {
                                latestState.originalSections
                            },
                        ).withSectionProgress(latestState.playbackState.positionMillis)
                            .withEditingSectionLearningContent()
                            .also(::saveStructure)
                    }

                    GuidedListeningResult.MissingApiKey -> latestState.copy(
                        isGuidanceLoading = false,
                        guidanceError = GuidanceError.MissingApiKey,
                    )

                    GuidedListeningResult.UnableToGenerate -> latestState.copy(
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
        val songKey = _uiState.value.importedSong?.uri ?: return
        songSessionCoordinator.saveStructure(
            scope = viewModelScope,
            songKey = songKey,
            originalSections = originalSections,
            editedSections = editedSections,
        )
    }

    private fun persistPlaybackPositionIfNeeded(playbackState: PlaybackState) {
        songSessionCoordinator.persistPositionIfNeeded(
            scope = viewModelScope,
            songKey = _uiState.value.importedSong?.uri,
            playbackState = playbackState,
        )
    }

    private fun saveCurrentSession() {
        val song = _uiState.value.importedSong ?: return
        songSessionCoordinator.markSaved(viewModelScope, song)
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
            editingSectionLearningContent = sections
                .firstOrNull { it.id == editingSectionId }
                ?.let { section ->
                    SectionExplanationProvider.contentFor(
                        label = section.label,
                        level = learningLevel,
                        status = section.status,
                    )
                },
        )
    }

    private companion object {
        const val GUIDED_REPEAT_MILLIS = 8_000L
    }
}
