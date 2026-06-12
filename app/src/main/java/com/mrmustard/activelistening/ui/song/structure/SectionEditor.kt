package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import com.mrmustard.activelistening.domain.structure.SectionRhythmInfo
import com.mrmustard.activelistening.domain.structure.SectionRhythmRegularity
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectionDetailsSheetContent(
    section: SongSection,
    learningContent: SectionLearningContent?,
    currentPositionMillis: Long,
    canSplitAtCurrentPosition: Boolean,
    canMergeWithPrevious: Boolean,
    canMergeWithNext: Boolean,
    onLabelSelected: (SectionLabel) -> Unit,
    onCustomLabelChanged: (String) -> Unit,
    onStatusClick: () -> Unit,
    onMusicalContrastClick: () -> Unit,
    onAdjustStart: (Long) -> Unit,
    onAdjustEnd: (Long) -> Unit,
    onSplitAtCurrentPosition: () -> Unit,
    onMergeWithPrevious: () -> Unit,
    onMergeWithNext: () -> Unit,
    onRepeatSection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var customLabelText by remember(section.id, section.customLabel) {
        mutableStateOf(section.customLabel.orEmpty())
    }
    val focusManager = LocalFocusManager.current
    fun commitCustomLabel() {
        if (customLabelText.trim() != section.customLabel.orEmpty()) {
            onCustomLabelChanged(customLabelText)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
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
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                HeaderTag(
                    text = section.status.toDisplayName(),
                    isActive = false,
                    onClick = onStatusClick,
                )
                HeaderTag(
                    text = stringResource(R.string.structure_contrast_chip),
                    isActive = section.musicalContrast != null,
                    onClick = onMusicalContrastClick,
                )
            }
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
        if (section.label == SectionLabel.Other) {
            OutlinedTextField(
                value = customLabelText,
                onValueChange = { customLabelText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) commitCustomLabel() },
                label = { Text(stringResource(R.string.structure_custom_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        commitCustomLabel()
                        focusManager.clearFocus()
                    },
                ),
            )
        }

        RhythmInfoPanel(
            rhythmInfo = section.rhythmInfo,
            musicalContrast = section.musicalContrast,
        )

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

        HorizontalDivider()

        Text(
            text = stringResource(R.string.structure_divisions_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Button(
            onClick = onSplitAtCurrentPosition,
            enabled = canSplitAtCurrentPosition,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(
                    R.string.structure_split_at_current_position,
                    formatSectionTime(currentPositionMillis),
                ),
            )
        }
        OutlinedButton(
            onClick = onRepeatSection,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.structure_repeat_section))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onMergeWithPrevious,
                enabled = canMergeWithPrevious,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.structure_merge_previous))
            }
            OutlinedButton(
                onClick = onMergeWithNext,
                enabled = canMergeWithNext,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.structure_merge_next))
            }
        }
    }
}

@Composable
private fun HeaderTag(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    FilterChip(
        selected = isActive,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            selectedContainerColor = containerColor,
            selectedLabelColor = contentColor,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isActive,
            borderColor = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
            selectedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun RhythmInfoPanel(
    rhythmInfo: SectionRhythmInfo?,
    musicalContrast: SectionMusicalContrast?,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.structure_rhythm_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            when {
                rhythmInfo?.regularity == SectionRhythmRegularity.Irregular -> Text(
                    text = stringResource(R.string.structure_rhythm_irregular),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                rhythmInfo?.estimatedBars != null -> Text(
                    text = stringResource(
                        R.string.structure_rhythm_estimated_bars,
                        rhythmInfo.estimatedBars,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> Text(
                    text = stringResource(R.string.structure_rhythm_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (rhythmInfo?.confidence == SectionRhythmConfidence.Low &&
                rhythmInfo.regularity != SectionRhythmRegularity.Irregular
            ) {
                Text(
                    text = stringResource(R.string.structure_rhythm_low_confidence),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            musicalContrast?.let { contrast ->
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.structure_contrast_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = contrast.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (contrast.confidence == SectionRhythmConfidence.Low) {
                    Text(
                        text = stringResource(R.string.structure_contrast_low_confidence),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
