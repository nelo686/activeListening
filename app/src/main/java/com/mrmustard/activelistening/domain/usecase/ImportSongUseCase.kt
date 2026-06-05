package com.mrmustard.activelistening.domain.usecase

import android.net.Uri
import com.mrmustard.activelistening.data.importing.SongImportRepository
import com.mrmustard.activelistening.data.playback.AudioPlaybackRepository
import com.mrmustard.activelistening.domain.SongImportResult
import javax.inject.Inject

class ImportSongUseCase @Inject constructor(
    private val songImportRepository: SongImportRepository,
    private val audioPlaybackRepository: AudioPlaybackRepository,
) {
    suspend operator fun invoke(uri: Uri): SongImportResult {
        val result = songImportRepository.importSong(uri)

        when (result) {
            is SongImportResult.Success -> audioPlaybackRepository.load(result.song)
            is SongImportResult.Error -> audioPlaybackRepository.pause()
        }

        return result
    }
}
