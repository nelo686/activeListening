package com.mrmustard.activelistening.ui.importsong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSongScreen(
    state: ImportSongUiState,
    onImportClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.importErrorMessage) {
        val message = state.importErrorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onErrorShown()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Escucha activa") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Header()
                ImportAction(
                    isImporting = state.isImporting,
                    hasSong = state.importedSong != null,
                    onImportClick = onImportClick,
                )

                val song = state.importedSong
                if (song == null) {
                    EmptySession()
                } else {
                    ListeningSession(
                        title = song.displayName,
                        playbackState = state.playbackState,
                        onPlayClick = onPlayClick,
                        onPauseClick = onPauseClick,
                        onSeek = onSeek,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportSongScreenPreview() {
    ActiveListeningTheme {
        ImportSongScreen(
            state = ImportSongUiState(),
            onImportClick = {},
            onPlayClick = {},
            onPauseClick = {},
            onSeek = {},
            onErrorShown = {},
        )
    }
}
