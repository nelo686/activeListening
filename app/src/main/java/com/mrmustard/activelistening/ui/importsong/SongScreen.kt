package com.mrmustard.activelistening.ui.importsong

import android.net.Uri
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.ImportedSong
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongScreen(
    state: ActiveListeningUiState,
    onImportClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onStartGuidedSession: () -> Unit,
    onConfirmGuidedMarker: () -> Unit,
    onMarkGuidedMarkerUncertain: () -> Unit,
    onSkipGuidedMarker: () -> Unit,
    onRepeatGuidedMarker: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val importErrorMessage = state.importError?.let { error -> error.toMessage() }

    LaunchedEffect(importErrorMessage) {
        val message = importErrorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onErrorShown()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.screen_import_song_title)) })
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
                        isGuidedSessionActive = state.isGuidedSessionActive,
                        isGuidanceLoading = state.isGuidanceLoading,
                        guidanceError = state.guidanceError,
                        guidedTimeline = state.guidedTimeline,
                        currentGuidedMarker = state.currentGuidedMarker,
                        onPlayClick = onPlayClick,
                        onPauseClick = onPauseClick,
                        onSeek = onSeek,
                        onStartGuidedSession = onStartGuidedSession,
                        onConfirmGuidedMarker = onConfirmGuidedMarker,
                        onMarkGuidedMarkerUncertain = onMarkGuidedMarkerUncertain,
                        onSkipGuidedMarker = onSkipGuidedMarker,
                        onRepeatGuidedMarker = onRepeatGuidedMarker,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SongScreenPreview() {
    ActiveListeningTheme {
        SongScreen(
            state = ActiveListeningUiState(),
            onImportClick = {},
            onPlayClick = {},
            onPauseClick = {},
            onSeek = {},
            onStartGuidedSession = {},
            onConfirmGuidedMarker = {},
            onMarkGuidedMarkerUncertain = {},
            onSkipGuidedMarker = {},
            onRepeatGuidedMarker = {},
            onErrorShown = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuidedSongScreenPreview() {
    val durationMillis = 180_000L
    val timeline = GuidedListeningTimelineFactory.create(durationMillis)

    ActiveListeningTheme {
        SongScreen(
            state = ActiveListeningUiState(
                importedSong = ImportedSong(
                    uri = Uri.EMPTY,
                    displayName = "Cancion de practica",
                    mimeType = "audio/mpeg",
                    durationMillis = durationMillis,
                ),
                playbackState = PlaybackState(
                    isReady = true,
                    positionMillis = 45_000L,
                    durationMillis = durationMillis,
                ),
                isGuidedSessionActive = true,
                guidedTimeline = timeline,
                currentGuidedMarker = timeline.getOrNull(1)?.copy(
                    status = GuidedListeningMarkerStatus.Current,
                ),
            ),
            onImportClick = {},
            onPlayClick = {},
            onPauseClick = {},
            onSeek = {},
            onStartGuidedSession = {},
            onConfirmGuidedMarker = {},
            onMarkGuidedMarkerUncertain = {},
            onSkipGuidedMarker = {},
            onRepeatGuidedMarker = {},
            onErrorShown = {},
        )
    }
}
