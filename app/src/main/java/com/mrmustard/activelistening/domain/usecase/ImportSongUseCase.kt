package com.mrmustard.activelistening.domain.usecase

import android.net.Uri
import com.mrmustard.activelistening.domain.importsong.SongImportGateway
import com.mrmustard.activelistening.domain.importsong.SongImportResult
import com.mrmustard.activelistening.domain.playback.AudioPlayer
import javax.inject.Inject

class ImportSongUseCase @Inject constructor(
    private val songImportGateway: SongImportGateway,
    private val audioPlayer: AudioPlayer,
) {
    suspend operator fun invoke(uri: Uri): SongImportResult {
        val result = songImportGateway.importSong(uri)

        when (result) {
            is SongImportResult.Success -> audioPlayer.load(result.song)
            is SongImportResult.Error -> audioPlayer.pause()
        }

        return result
    }
}
