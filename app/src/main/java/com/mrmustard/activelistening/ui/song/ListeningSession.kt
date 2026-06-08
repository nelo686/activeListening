package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.ui.song.structure.SectionEditor
import com.mrmustard.activelistening.ui.song.structure.StructureTimeline

@Composable
fun ListeningSession(
    playbackState: PlaybackState,
    isGuidedSessionActive: Boolean,
    isGuidanceLoading: Boolean,
    guidanceError: GuidanceError?,
    sections: List<SongSection>,
    selectedSectionId: Int?,
    activeSectionId: Int?,
    guidanceIntensity: GuidanceIntensity,
    isSectionDetailsExpanded: Boolean,
    selectedSectionLearningContent: SectionLearningContent?,
    onStartGuidedSession: () -> Unit,
    onSectionSelected: (Int) -> Unit,
    onSectionLabelSelected: (SectionLabel) -> Unit,
    onConfirmSection: () -> Unit,
    onMarkSectionUncertain: () -> Unit,
    onRepeatGuidedMarker: () -> Unit,
    onAdjustSectionStart: (Long) -> Unit,
    onAdjustSectionEnd: (Long) -> Unit,
    onToggleSectionDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
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
                SectionEditor(
                    section = sections.firstOrNull { it.id == selectedSectionId },
                    isGuidanceLoading = isGuidanceLoading,
                    guidanceError = guidanceError,
                    guidanceIntensity = guidanceIntensity,
                    isSectionDetailsExpanded = isSectionDetailsExpanded,
                    learningContent = selectedSectionLearningContent,
                    onToggleSectionDetails = onToggleSectionDetails,
                    onLabelSelected = onSectionLabelSelected,
                    onConfirmClick = onConfirmSection,
                    onUncertainClick = onMarkSectionUncertain,
                    onRepeatClick = onRepeatGuidedMarker,
                    onAdjustStart = onAdjustSectionStart,
                    onAdjustEnd = onAdjustSectionEnd,
                )
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
}
