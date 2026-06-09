package com.mrmustard.activelistening.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrmustard.activelistening.ui.config.ConfigScreen
import com.mrmustard.activelistening.ui.song.SongScreen
import com.mrmustard.activelistening.ui.song.ActiveListeningViewModel
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ActiveListeningViewModel by viewModels()
    private lateinit var openSongLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var createMapPdfLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        openSongLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let(::handleSelectedSong)
        }
        createMapPdfLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/pdf"),
        ) { uri ->
            uri?.let(viewModel::exportMap)
        }

        setContent {
            ActiveListeningTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                var currentScreen by remember { mutableStateOf(MainScreen.Song) }

                BackHandler(enabled = currentScreen == MainScreen.Config) {
                    currentScreen = MainScreen.Song
                }

                when (currentScreen) {
                    MainScreen.Song -> SongScreen(
                        state = state,
                        onImportClick = { openSongLauncher.launch(arrayOf("*/*")) },
                        onSavedSessionClick = viewModel::resumeSavedSession,
                        onBackToStartClick = viewModel::returnToStart,
                        onSettingsClick = { currentScreen = MainScreen.Config },
                        onPlayClick = viewModel::play,
                        onPauseClick = viewModel::pause,
                        onSeek = viewModel::seekTo,
                        onStartGuidedSession = viewModel::startGuidedSession,
                        onSectionSelected = viewModel::openSectionEditor,
                        onSectionEditorDismiss = viewModel::closeSectionEditor,
                        onSectionLabelSelected = viewModel::changeSelectedSectionLabel,
                        onAdjustSectionStart = viewModel::setSelectedSectionStart,
                        onAdjustSectionEnd = viewModel::setSelectedSectionEnd,
                        onSplitAtCurrentPosition = viewModel::splitAtCurrentPosition,
                        onMergeWithPrevious = viewModel::mergeSelectedSectionWithPrevious,
                        onMergeWithNext = viewModel::mergeSelectedSectionWithNext,
                        onRestoreOriginalProposal = viewModel::restoreOriginalProposal,
                        onExportMapClick = {
                            createMapPdfLauncher.launch(state.exportFileName())
                        },
                        onErrorShown = viewModel::clearError,
                        onMapExportMessageShown = viewModel::clearMapExportMessage,
                    )

                    MainScreen.Config -> ConfigScreen(
                        learningLevel = state.learningLevel,
                        guidanceIntensity = state.guidanceIntensity,
                        onLearningLevelSelected = viewModel::changeLearningLevel,
                        onGuidanceIntensitySelected = viewModel::changeGuidanceIntensity,
                        onBackClick = { currentScreen = MainScreen.Song },
                    )
                }
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

private fun com.mrmustard.activelistening.ui.song.ActiveListeningUiState.exportFileName(): String {
    val baseName = importedSong?.displayName
        ?.substringBeforeLast('.')
        ?.takeIf { it.isNotBlank() }
        ?: "mapa-estructural"
    val sanitized = baseName
        .replace(Regex("[^A-Za-z0-9._-]+"), "-")
        .trim('-')
        .takeIf { it.isNotBlank() }
        ?: "mapa-estructural"
    return "$sanitized.pdf"
}

private enum class MainScreen {
    Song,
    Config,
}
