package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.ui.song.structure.SectionEditor
import com.mrmustard.activelistening.ui.song.structure.StructureTimeline

@Composable
fun ListeningSession(
    title: String,
    playbackState: PlaybackState,
    isGuidedSessionActive: Boolean,
    isGuidanceLoading: Boolean,
    guidanceError: GuidanceError?,
    sections: List<SongSection>,
    selectedSectionId: Int?,
    activeSectionId: Int?,
    guidanceIntensity: GuidanceIntensity,
    learningLevel: LearningLevel,
    isSectionDetailsExpanded: Boolean,
    selectedSectionLearningContent: SectionLearningContent?,
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
    onGuidanceIntensitySelected: (GuidanceIntensity) -> Unit,
    onLearningLevelSelected: (LearningLevel) -> Unit,
    onToggleSectionDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.import_song_listening_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PlaybackControls(
                playbackState = playbackState,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
                onSeek = onSeek,
            )

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
                    learningLevel = learningLevel,
                    isSectionDetailsExpanded = isSectionDetailsExpanded,
                    learningContent = selectedSectionLearningContent,
                    onGuidanceIntensitySelected = onGuidanceIntensitySelected,
                    onLearningLevelSelected = onLearningLevelSelected,
                    onToggleSectionDetails = onToggleSectionDetails,
                    onLabelSelected = onSectionLabelSelected,
                    onConfirmClick = onConfirmSection,
                    onUncertainClick = onMarkSectionUncertain,
                    onRepeatClick = onRepeatGuidedMarker,
                    onAdjustStart = onAdjustSectionStart,
                    onAdjustEnd = onAdjustSectionEnd,
                )
            } else {
                Button(
                    onClick = onStartGuidedSession,
                    enabled = playbackState.durationMillis > 0L,
                ) {
                    Text(stringResource(R.string.guided_listening_start))
                }
            }
        }
    }
}
