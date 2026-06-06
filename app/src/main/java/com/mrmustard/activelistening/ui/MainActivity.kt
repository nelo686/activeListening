package com.mrmustard.activelistening.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrmustard.activelistening.ui.song.SongScreen
import com.mrmustard.activelistening.ui.song.ActiveListeningViewModel
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ActiveListeningViewModel by viewModels()
    private lateinit var openSongLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        openSongLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let(::handleSelectedSong)
        }

        setContent {
            ActiveListeningTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                SongScreen(
                    state = state,
                    onImportClick = { openSongLauncher.launch(arrayOf("*/*")) },
                    onPlayClick = viewModel::play,
                    onPauseClick = viewModel::pause,
                    onSeek = viewModel::seekTo,
                    onStartGuidedSession = viewModel::startGuidedSession,
                    onConfirmGuidedMarker = viewModel::confirmGuidedMarker,
                    onMarkGuidedMarkerUncertain = viewModel::markGuidedMarkerUncertain,
                    onSkipGuidedMarker = viewModel::skipGuidedMarker,
                    onRepeatGuidedMarker = viewModel::repeatGuidedMarker,
                    onErrorShown = viewModel::clearError,
                )
            }
        }
    }

    private fun handleSelectedSong(uri: Uri) {
        persistReadPermission(uri)
        viewModel.importSong(uri)
    }

    private fun persistReadPermission(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }
}
