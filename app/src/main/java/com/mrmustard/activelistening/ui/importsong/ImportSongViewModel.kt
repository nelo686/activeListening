package com.mrmustard.activelistening.ui.importsong

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrmustard.activelistening.data.playback.AudioPlaybackRepository
import com.mrmustard.activelistening.domain.SongImportResult
import com.mrmustard.activelistening.domain.usecase.ImportSongUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ImportSongViewModel @Inject constructor(
    private val importSongUseCase: ImportSongUseCase,
    private val audioPlaybackRepository: AudioPlaybackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportSongUiState())
    val uiState: StateFlow<ImportSongUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            audioPlaybackRepository.playbackState.collect { playbackState ->
                _uiState.update { it.copy(playbackState = playbackState) }
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

    fun pause() {
        audioPlaybackRepository.pause()
    }

    fun seekTo(positionMillis: Long) {
        audioPlaybackRepository.seekTo(positionMillis)
    }

    fun clearError() {
        _uiState.update { it.copy(importError = null) }
    }

    override fun onCleared() {
        audioPlaybackRepository.release()
        super.onCleared()
    }

}
