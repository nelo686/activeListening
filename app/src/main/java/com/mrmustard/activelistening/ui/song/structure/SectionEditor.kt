package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectionDetailsSheetContent(
    section: SongSection,
    learningContent: SectionLearningContent?,
    onLabelSelected: (SectionLabel) -> Unit,
    onAdjustStart: (Long) -> Unit,
    onAdjustEnd: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.structure_editor_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${formatSectionTime(section.startMillis)}-${formatSectionTime(section.endMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(section.status.toDisplayName()) },
            )
        }

        Text(
            text = stringResource(R.string.structure_label_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionLabel.entries.forEach { label ->
                FilterChip(
                    selected = section.label == label,
                    onClick = { onLabelSelected(label) },
                    label = { Text(label.toDisplayName()) },
                )
            }
        }

        LearningPanel(content = learningContent)

        HorizontalDivider()

        BoundaryEditor(
            title = stringResource(R.string.structure_adjust_start),
            suggestedTimeMillis = section.startMillis,
            onChange = onAdjustStart,
        )
        BoundaryEditor(
            title = stringResource(R.string.structure_adjust_end),
            suggestedTimeMillis = section.endMillis,
            onChange = onAdjustEnd,
        )
    }
}
