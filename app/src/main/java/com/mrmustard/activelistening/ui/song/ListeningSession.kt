package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.ui.song.structure.SectionDetailsSheetContent
import com.mrmustard.activelistening.ui.song.structure.StructureTimeline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningSession(
    playbackState: PlaybackState,
    isGuidedSessionActive: Boolean,
    isGuidanceLoading: Boolean,
    guidanceError: GuidanceError?,
    sections: List<SongSection>,
    selectedSectionId: Int?,
    activeSectionId: Int?,
    editingSectionId: Int?,
    editingSectionLearningContent: SectionLearningContent?,
    canRestoreOriginalProposal: Boolean,
    canExportMap: Boolean,
    isExportingMap: Boolean,
    guidanceIntensity: GuidanceIntensity,
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
    modifier: Modifier = Modifier,
) {
    val sheetSection = sections.firstOrNull { it.id == editingSectionId }
    val sheetSectionIndex = sections.indexOfFirst { it.id == editingSectionId }
    val canSplitAtCurrentPosition = sheetSection?.let { section ->
        playbackState.positionMillis - section.startMillis >= SongStructureFactory.MIN_SECTION_DURATION_MILLIS &&
            section.endMillis - playbackState.positionMillis >= SongStructureFactory.MIN_SECTION_DURATION_MILLIS
    } ?: false
    val guidanceSection = sections.firstOrNull { it.id == activeSectionId }
        ?: sections.firstOrNull { it.id == selectedSectionId }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (isGuidedSessionActive) {
                StructureTimeline(
                    sections = sections,
                    selectedSectionId = selectedSectionId,
                    activeSectionId = activeSectionId,
                    positionMillis = playbackState.positionMillis,
                    durationMillis = playbackState.durationMillis,
                    onSectionClick = onSectionSelected,
                )
                GuidedPromptPanel(
                    section = guidanceSection,
                    isGuidanceLoading = isGuidanceLoading,
                    guidanceError = guidanceError,
                    guidanceIntensity = guidanceIntensity,
                    onConfirm = onConfirmGuidedSection,
                    onMarkUncertain = onMarkGuidedSectionUncertain,
                    onRepeat = onRepeatGuidedPrompt,
                    onSkip = onSkipGuidedSection,
                )
                if (canRestoreOriginalProposal) {
                    OutlinedButton(
                        onClick = onRestoreOriginalProposal,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.structure_restore_original))
                    }
                }
                OutlinedButton(
                    onClick = onExportMapClick,
                    enabled = canExportMap && !isExportingMap,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(
                            if (isExportingMap) {
                                R.string.map_export_generating
                            } else {
                                R.string.map_export_action
                            },
                        ),
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.import_song_listening_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onStartGuidedSession,
                    enabled = playbackState.durationMillis > 0L,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(stringResource(R.string.guided_listening_start))
                }
            }
        }
    }

    if (sheetSection != null) {
        ModalBottomSheet(
            onDismissRequest = onSectionEditorDismiss,
        ) {
            SectionDetailsSheetContent(
                section = sheetSection,
                learningContent = editingSectionLearningContent,
                currentPositionMillis = playbackState.positionMillis,
                canSplitAtCurrentPosition = canSplitAtCurrentPosition,
                canMergeWithPrevious = sheetSectionIndex > 0,
                canMergeWithNext = sheetSectionIndex in 0 until sections.lastIndex,
                onLabelSelected = onSectionLabelSelected,
                onCustomLabelChanged = onSectionCustomLabelChanged,
                onStatusClick = onSectionStatusClick,
                onMusicalContrastClick = onSectionMusicalContrastClick,
                onAdjustStart = onAdjustSectionStart,
                onAdjustEnd = onAdjustSectionEnd,
                onSplitAtCurrentPosition = onSplitAtCurrentPosition,
                onMergeWithPrevious = onMergeWithPrevious,
                onMergeWithNext = onMergeWithNext,
                onRepeatSection = onRepeatSection,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}
