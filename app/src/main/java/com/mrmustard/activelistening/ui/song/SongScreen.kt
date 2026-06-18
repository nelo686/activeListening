package com.mrmustard.activelistening.ui.song

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import com.mrmustard.activelistening.ui.song.importsong.toMessage
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongScreen(
    state: ActiveListeningUiState,
    onImportClick: () -> Unit,
    onSavedSessionClick: (SavedListeningSession) -> Unit,
    onDeleteSavedSession: (String) -> Unit,
    onUndoSavedSessionDeletion: () -> Unit,
    onSavedSessionDeletionMessageShown: () -> Unit,
    onBackToStartClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onStartGuidedSession: () -> Unit,
    onSectionSelected: (Int) -> Unit,
    onSectionEditorDismiss: () -> Unit,
    onSectionLabelSelected: (SectionLabel) -> Unit,
    onSectionCustomLabelChanged: (String) -> Unit,
    onSectionStatusClick: () -> Unit,
    onSectionMusicalContrastClick: () -> Unit,
    onAdjustSectionStart: (Long) -> Unit,
    onAdjustSectionEnd: (Long) -> Unit,
    onSplitAtCurrentPosition: () -> Unit,
    onMergeWithPrevious: () -> Unit,
    onMergeWithNext: () -> Unit,
    onRepeatSection: () -> Unit,
    onConfirmGuidedSection: () -> Unit,
    onMarkGuidedSectionUncertain: () -> Unit,
    onRepeatGuidedPrompt: () -> Unit,
    onSkipGuidedSection: () -> Unit,
    onRestoreOriginalProposal: () -> Unit,
    onExportMapClick: () -> Unit,
    onErrorShown: () -> Unit,
    onMapExportMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var openSavedSessionKey by rememberSaveable { mutableStateOf<String?>(null) }
    val importErrorMessage = state.importError?.let { error -> error.toMessage() }
    val mapExportMessage = state.mapExportError?.let { error -> error.toMessage() }
        ?: state.exportedMapFileName?.let {
            stringResource(R.string.map_export_success)
        }
    val deletionEvent = state.savedSessionDeletionEvent
    val deletionMessage = deletionEvent?.let { event ->
        if (event.isError) {
            stringResource(R.string.saved_sessions_delete_error)
        } else {
            stringResource(R.string.saved_sessions_deleted, event.deletedDisplayName.orEmpty())
        }
    }
    val undoLabel = stringResource(R.string.saved_sessions_undo)

    LaunchedEffect(importErrorMessage) {
        val message = importErrorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onErrorShown()
    }

    LaunchedEffect(mapExportMessage) {
        val message = mapExportMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMapExportMessageShown()
    }

    LaunchedEffect(deletionEvent?.id) {
        val event = deletionEvent ?: return@LaunchedEffect
        val message = deletionMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = undoLabel.takeUnless { event.isError },
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) {
            onUndoSavedSessionDeletion()
        }
        onSavedSessionDeletionMessageShown()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_import_song_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    if (state.importedSong != null) {
                        IconButton(onClick = onBackToStartClick) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back_24),
                                contentDescription = stringResource(R.string.song_back_to_start),
                            )
                        }
                    }
                },
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
            color = MaterialTheme.colorScheme.background,
        ) {
            val song = state.importedSong
            if (song == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        top = 10.dp,
                        end = 12.dp,
                        bottom = 42.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    item {
                        EmptySession(
                            isImporting = state.isImporting,
                            onImportClick = onImportClick,
                        )
                    }
                    savedSessions(
                        sessions = state.savedSessions,
                        progressSummaries = state.progressSummaries,
                        onSessionClick = onSavedSessionClick,
                        onDeleteSession = onDeleteSavedSession,
                        openSessionKey = openSavedSessionKey,
                        onOpenSessionChange = { openSavedSessionKey = it },
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    SongPlayerHeader(
                        song = song,
                        playbackState = state.playbackState,
                        onPlayClick = onPlayClick,
                        onPauseClick = onPauseClick,
                        onSeek = onSeek,
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .imePadding(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                    item {
                        ListeningSession(
                            playbackState = state.playbackState,
                            isGuidedSessionActive = state.isGuidedSessionActive,
                            isGuidanceLoading = state.isGuidanceLoading,
                            guidanceError = state.guidanceError,
                            sections = state.sections,
                            selectedSectionId = state.selectedSectionId,
                            activeSectionId = state.activeSectionId,
                            editingSectionId = state.editingSectionId,
                            editingSectionLearningContent = state.editingSectionLearningContent,
                            canRestoreOriginalProposal = state.canRestoreOriginalProposal,
                            canExportMap = state.canExportMap,
                            isExportingMap = state.isExportingMap,
                            guidanceIntensity = state.guidanceIntensity,
                            onStartGuidedSession = onStartGuidedSession,
                            onSectionSelected = onSectionSelected,
                            onSectionEditorDismiss = onSectionEditorDismiss,
                            onSectionLabelSelected = onSectionLabelSelected,
                            onSectionCustomLabelChanged = onSectionCustomLabelChanged,
                            onSectionStatusClick = onSectionStatusClick,
                            onSectionMusicalContrastClick = onSectionMusicalContrastClick,
                            onAdjustSectionStart = onAdjustSectionStart,
                            onAdjustSectionEnd = onAdjustSectionEnd,
                            onSplitAtCurrentPosition = onSplitAtCurrentPosition,
                            onMergeWithPrevious = onMergeWithPrevious,
                            onMergeWithNext = onMergeWithNext,
                            onRepeatSection = onRepeatSection,
                            onConfirmGuidedSection = onConfirmGuidedSection,
                            onMarkGuidedSectionUncertain = onMarkGuidedSectionUncertain,
                            onRepeatGuidedPrompt = onRepeatGuidedPrompt,
                            onSkipGuidedSection = onSkipGuidedSection,
                            onRestoreOriginalProposal = onRestoreOriginalProposal,
                            onExportMapClick = onExportMapClick,
                        )
                    }
                    }
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
            onSavedSessionClick = {},
            onDeleteSavedSession = {},
            onUndoSavedSessionDeletion = {},
            onSavedSessionDeletionMessageShown = {},
            onBackToStartClick = {},
            onSettingsClick = {},
            onPlayClick = {},
            onPauseClick = {},
            onSeek = {},
            onStartGuidedSession = {},
            onSectionSelected = {},
            onSectionEditorDismiss = {},
            onSectionLabelSelected = {},
            onSectionCustomLabelChanged = {},
            onSectionStatusClick = {},
            onSectionMusicalContrastClick = {},
            onAdjustSectionStart = {},
            onAdjustSectionEnd = {},
            onSplitAtCurrentPosition = {},
            onMergeWithPrevious = {},
            onMergeWithNext = {},
            onRepeatSection = {},
            onConfirmGuidedSection = {},
            onMarkGuidedSectionUncertain = {},
            onRepeatGuidedPrompt = {},
            onSkipGuidedSection = {},
            onRestoreOriginalProposal = {},
            onExportMapClick = {},
            onErrorShown = {},
            onMapExportMessageShown = {},
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
            onSavedSessionClick = {},
            onDeleteSavedSession = {},
            onUndoSavedSessionDeletion = {},
            onSavedSessionDeletionMessageShown = {},
            onBackToStartClick = {},
            onSettingsClick = {},
            onPlayClick = {},
            onPauseClick = {},
            onSeek = {},
            onStartGuidedSession = {},
            onSectionSelected = {},
            onSectionEditorDismiss = {},
            onSectionLabelSelected = {},
            onSectionCustomLabelChanged = {},
            onSectionStatusClick = {},
            onSectionMusicalContrastClick = {},
            onAdjustSectionStart = {},
            onAdjustSectionEnd = {},
            onSplitAtCurrentPosition = {},
            onMergeWithPrevious = {},
            onMergeWithNext = {},
            onRepeatSection = {},
            onConfirmGuidedSection = {},
            onMarkGuidedSectionUncertain = {},
            onRepeatGuidedPrompt = {},
            onSkipGuidedSection = {},
            onRestoreOriginalProposal = {},
            onExportMapClick = {},
            onErrorShown = {},
            onMapExportMessageShown = {},
        )
    }
}

@Composable
private fun MapExportError.toMessage(): String =
    when (this) {
        MapExportError.InsufficientStructure -> stringResource(R.string.map_export_error_incomplete)
        MapExportError.UnableToWrite -> stringResource(R.string.map_export_error_unable_to_write)
    }
