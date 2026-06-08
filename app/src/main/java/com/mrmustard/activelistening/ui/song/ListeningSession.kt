package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.ui.song.structure.SectionDetailsSheetContent
import com.mrmustard.activelistening.ui.song.structure.StructureTimeline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningSession(
    playbackState: PlaybackState,
    isGuidedSessionActive: Boolean,
    sections: List<SongSection>,
    selectedSectionId: Int?,
    activeSectionId: Int?,
    selectedSectionLearningContent: SectionLearningContent?,
    onStartGuidedSession: () -> Unit,
    onSectionSelected: (Int) -> Unit,
    onSectionLabelSelected: (SectionLabel) -> Unit,
    onAdjustSectionStart: (Long) -> Unit,
    onAdjustSectionEnd: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sheetSectionId by remember { mutableStateOf<Int?>(null) }
    val sheetSection = sections.firstOrNull { it.id == sheetSectionId }
    val sheetLearningContent = selectedSectionLearningContent
        .takeIf { selectedSectionId == sheetSectionId }

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
                    onSectionClick = { sectionId ->
                        sheetSectionId = sectionId
                        onSectionSelected(sectionId)
                    },
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

    if (sheetSection != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetSectionId = null },
        ) {
            SectionDetailsSheetContent(
                section = sheetSection,
                learningContent = sheetLearningContent,
                onLabelSelected = onSectionLabelSelected,
                onAdjustStart = onAdjustSectionStart,
                onAdjustEnd = onAdjustSectionEnd,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}
