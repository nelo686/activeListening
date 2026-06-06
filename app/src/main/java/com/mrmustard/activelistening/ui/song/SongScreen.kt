package com.mrmustard.activelistening.ui.song

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import com.mrmustard.activelistening.ui.song.importsong.ImportAction
import com.mrmustard.activelistening.ui.song.importsong.toMessage
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongScreen(
    state: ActiveListeningUiState,
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onStartGuidedSession: () -> Unit,
    onSectionSelected: (Int) -> Unit,
    onSectionLabelSelected: (SectionLabel) -> Unit,
    onConfirmSection: () -> Unit,
    onMarkSectionUncertain: () -> Unit,
    onRepeatGuidedMarker: () -> Unit,
    onAdjustSectionStart: (Long) -> Unit,
    onAdjustSectionEnd: (Long) -> Unit,
    onToggleSectionDetails: () -> Unit,
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
            TopAppBar(
                title = { Text(stringResource(R.string.screen_import_song_title)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_24),
                            contentDescription = stringResource(R.string.settings_open),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val song = state.importedSong
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (song == null) {
                    item { Header() }
                    item {
                        ImportAction(
                            isImporting = state.isImporting,
                            hasSong = false,
                            onImportClick = onImportClick,
                        )
                    }
                    item { EmptySession() }
                } else {
                    item { ImportedSongHeader() }
                    item {
                        ImportAction(
                            isImporting = state.isImporting,
                            hasSong = true,
                            onImportClick = onImportClick,
                        )
                    }
                    item {
                        ListeningSession(
                            title = song.displayName,
                            playbackState = state.playbackState,
                            isGuidedSessionActive = state.isGuidedSessionActive,
                            isGuidanceLoading = state.isGuidanceLoading,
                            guidanceError = state.guidanceError,
                            sections = state.sections,
                            selectedSectionId = state.selectedSectionId,
                            activeSectionId = state.activeSectionId,
                            guidanceIntensity = state.guidanceIntensity,
                            isSectionDetailsExpanded = state.isSectionDetailsExpanded,
                            selectedSectionLearningContent = state.selectedSectionLearningContent,
                            onPlayClick = onPlayClick,
                            onPauseClick = onPauseClick,
                            onSeek = onSeek,
                            onStartGuidedSession = onStartGuidedSession,
                            onSectionSelected = onSectionSelected,
                            onSectionLabelSelected = onSectionLabelSelected,
                            onConfirmSection = onConfirmSection,
                            onMarkSectionUncertain = onMarkSectionUncertain,
                            onRepeatGuidedMarker = onRepeatGuidedMarker,
                            onAdjustSectionStart = onAdjustSectionStart,
                            onAdjustSectionEnd = onAdjustSectionEnd,
                            onToggleSectionDetails = onToggleSectionDetails,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportedSongHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.import_song_ready_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.import_song_ready_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SongScreenPreview() {
    ActiveListeningTheme {
        SongScreen(
            state = ActiveListeningUiState(),
            onImportClick = {},
            onSettingsClick = {},
            onPlayClick = {},
            onPauseClick = {},
            onSeek = {},
            onStartGuidedSession = {},
            onSectionSelected = {},
            onSectionLabelSelected = {},
            onConfirmSection = {},
            onMarkSectionUncertain = {},
            onRepeatGuidedMarker = {},
            onAdjustSectionStart = {},
            onAdjustSectionEnd = {},
            onToggleSectionDetails = {},
            onErrorShown = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuidedSongScreenPreview() {
    val durationMillis = 180_000L
    val sections = SongStructureFactory.createInitialSections(durationMillis)

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
                sections = sections,
                selectedSectionId = sections.getOrNull(1)?.id,
                activeSectionId = sections.getOrNull(1)?.id,
            ),
            onImportClick = {},
            onSettingsClick = {},
            onPlayClick = {},
            onPauseClick = {},
            onSeek = {},
            onStartGuidedSession = {},
            onSectionSelected = {},
            onSectionLabelSelected = {},
            onConfirmSection = {},
            onMarkSectionUncertain = {},
            onRepeatGuidedMarker = {},
            onAdjustSectionStart = {},
            onAdjustSectionEnd = {},
            onToggleSectionDetails = {},
            onErrorShown = {},
        )
    }
}
