package com.mrmustard.activelistening.ui.song

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrmustard.activelistening.data.guidance.GuidedListeningMarkerRequest
import com.mrmustard.activelistening.data.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.data.guidance.GuidedListeningRequest
import com.mrmustard.activelistening.data.guidance.GuidedListeningResult
import com.mrmustard.activelistening.data.playback.AudioPlaybackRepository
import com.mrmustard.activelistening.domain.ImportedSong
import com.mrmustard.activelistening.domain.SongImportResult
import com.mrmustard.activelistening.domain.usecase.ImportSongUseCase
import com.mrmustard.activelistening.ui.song.guide.GuidedListeningMarker
import com.mrmustard.activelistening.ui.song.guide.GuidedListeningMarkerStatus
import com.mrmustard.activelistening.ui.song.guide.GuidedListeningTimelineFactory
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
                        .withGuidedProgress(playbackState.positionMillis)
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
                            guidedTimeline = emptyList(),
                            currentGuidedMarker = null,
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

            val timeline = GuidedListeningTimelineFactory.create(durationMillis)
            state.copy(
                isGuidedSessionActive = true,
                isGuidanceLoading = true,
                guidanceError = null,
                guidedTimeline = timeline,
                currentGuidedMarker = timeline.firstOrNull()?.copy(
                    status = GuidedListeningMarkerStatus.Current,
                ),
            ).withGuidedProgress(state.playbackState.positionMillis)
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

    fun confirmGuidedMarker() {
        updateCurrentMarkerStatus(GuidedListeningMarkerStatus.Reviewed)
    }

    fun markGuidedMarkerUncertain() {
        updateCurrentMarkerStatus(GuidedListeningMarkerStatus.Uncertain)
    }

    fun skipGuidedMarker() {
        updateCurrentMarkerStatus(GuidedListeningMarkerStatus.Skipped)
    }

    fun repeatGuidedMarker() {
        val marker = _uiState.value.currentGuidedMarker ?: return
        audioPlaybackRepository.seekTo((marker.positionMillis - REPEAT_OFFSET_MILLIS).coerceAtLeast(0L))
        audioPlaybackRepository.play()
    }

    fun clearError() {
        _uiState.update { it.copy(importError = null) }
    }

    override fun onCleared() {
        audioPlaybackRepository.release()
        super.onCleared()
    }

    private fun updateCurrentMarkerStatus(status: GuidedListeningMarkerStatus) {
        _uiState.update { state ->
            val currentMarker = state.currentGuidedMarker ?: return@update state
            val timeline = state.guidedTimeline.map { marker ->
                if (marker.id == currentMarker.id) marker.copy(status = status) else marker
            }
            state.copy(
                guidedTimeline = timeline,
                currentGuidedMarker = timeline.firstOrNull { it.id == currentMarker.id },
            )
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
                    markers = state.guidedTimeline.map { marker ->
                        GuidedListeningMarkerRequest(
                            id = marker.id,
                            positionMillis = marker.positionMillis,
                            title = marker.title,
                            prompt = marker.prompt,
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
                            guidedTimeline = currentState.guidedTimeline.merge(result),
                        ).withGuidedProgress(currentState.playbackState.positionMillis)
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

    private fun List<GuidedListeningMarker>.merge(
        result: GuidedListeningResult.Success,
    ): List<GuidedListeningMarker> {
        val suggestions = result.markers.associateBy { it.id }
        return map { marker ->
            val suggestion = suggestions[marker.id] ?: return@map marker
            marker.copy(
                title = suggestion.title,
                prompt = suggestion.prompt,
            )
        }
    }

    private fun ActiveListeningUiState.withGuidedProgress(positionMillis: Long): ActiveListeningUiState {
        if (!isGuidedSessionActive || guidedTimeline.isEmpty()) return this

        val currentMarker = guidedTimeline.lastOrNull { it.positionMillis <= positionMillis }
            ?: guidedTimeline.first()
        val updatedTimeline = guidedTimeline.map { marker ->
            when {
                marker.id == currentMarker.id &&
                    marker.status == GuidedListeningMarkerStatus.Pending ->
                    marker.copy(status = GuidedListeningMarkerStatus.Current)

                marker.id != currentMarker.id &&
                    marker.status == GuidedListeningMarkerStatus.Current ->
                    marker.copy(status = GuidedListeningMarkerStatus.Pending)

                else -> marker
            }
        }
        return copy(
            guidedTimeline = updatedTimeline,
            currentGuidedMarker = updatedTimeline.firstOrNull { it.id == currentMarker.id },
        )
    }

    private companion object {
        const val REPEAT_OFFSET_MILLIS = 8_000L
    }
}
