package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.learning.SectionLearningContent
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import com.mrmustard.activelistening.domain.structure.SectionRhythmInfo
import com.mrmustard.activelistening.domain.structure.SectionRhythmRegularity
import com.mrmustard.activelistening.domain.structure.SongSection

private val SheetSectionShape = RoundedCornerShape(18.dp)

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
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SheetHeader(
            section = section,
            onStatusClick = onStatusClick,
            onMusicalContrastClick = onMusicalContrastClick,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

        TimeAdjustmentCard(
            startMillis = section.startMillis,
            endMillis = section.endMillis,
            onAdjustStart = onAdjustStart,
            onAdjustEnd = onAdjustEnd,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading(stringResource(R.string.structure_label_title))
            SectionLabel.entries
                .filterNot { it == SectionLabel.Other }
                .chunked(2)
                .forEach { rowLabels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowLabels.forEach { label ->
                            LabelOptionCard(
                                label = label,
                                selected = section.label == label,
                                onClick = { onLabelSelected(label) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowLabels.size == 1) Box(Modifier.weight(1f))
                    }
                }

            CustomLabelOption(
                selected = section.label == SectionLabel.Other,
                onClick = { onLabelSelected(SectionLabel.Other) },
            )

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
        }

        LearningPanel(
            content = learningContent,
            sectionName = section.toDisplayName(),
        )

        RhythmInfoPanel(
            rhythmInfo = section.rhythmInfo,
            musicalContrast = section.musicalContrast,
        )

        HorizontalDivider()

        SectionHeading(stringResource(R.string.structure_divisions_title))
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
private fun SheetHeader(
    section: SongSection,
    onStatusClick: () -> Unit,
    onMusicalContrastClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.structure_editor_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = stringResource(R.string.structure_editor_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
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
}

@Composable
private fun TimeAdjustmentCard(
    startMillis: Long,
    endMillis: Long,
    onAdjustStart: (Long) -> Unit,
    onAdjustEnd: (Long) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = SheetSectionShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeading(stringResource(R.string.structure_time_adjustment_title))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompactTimeField(
                    label = stringResource(R.string.structure_time_start),
                    timeMillis = startMillis,
                    onApply = onAdjustStart,
                    modifier = Modifier.weight(1f),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                        ),
                    ) {
                        Text(
                            text = formatSectionTime((endMillis - startMillis).coerceAtLeast(0L)),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                CompactTimeField(
                    label = stringResource(R.string.structure_time_end),
                    timeMillis = endMillis,
                    onApply = onAdjustEnd,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CompactTimeField(
    label: String,
    timeMillis: Long,
    onApply: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember(timeMillis, label) { mutableStateOf(formatSectionTime(timeMillis)) }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(timeMillis) { text = formatSectionTime(timeMillis) }
    val parsedTime = parseEditorTime(text)
    val isInvalid = text.isNotBlank() && parsedTime == null

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            isError = isInvalid,
            label = { Text(label) },
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    parsedTime?.let(onApply)
                    keyboardController?.hide()
                },
            ),
            modifier = Modifier.onFocusChanged { state ->
                if (!state.isFocused && parsedTime != null && parsedTime != timeMillis) {
                    onApply(parsedTime)
                }
            },
        )
        Text(
            text = stringResource(
                if (isInvalid) R.string.structure_time_input_error else R.string.structure_time_input_hint,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = if (isInvalid) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LabelOptionCard(
    label: SectionLabel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
    else Color.Transparent
    val background = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)

    Surface(
        modifier = modifier
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape),
        shape = shape,
        color = background,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(24.dp)
                        .background(label.sectionColor(), RoundedCornerShape(4.dp)),
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                text = label.toDisplayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CustomLabelOption(
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            ),
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Text(
            text = "+  ${stringResource(R.string.structure_custom_label_action)}",
            modifier = Modifier.padding(vertical = 18.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun parseEditorTime(input: String): Long? {
    val parts = input.trim().split(":")
    if (parts.size != 2) return null
    val minutes = parts[0].toLongOrNull() ?: return null
    val seconds = parts[1].toLongOrNull() ?: return null
    if (minutes < 0L || seconds !in 0L..59L) return null
    return minutes * 60_000L + seconds * 1_000L
}

@Composable
private fun HeaderTag(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surface
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant
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
            borderColor = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun RhythmInfoPanel(
    rhythmInfo: SectionRhythmInfo?,
    musicalContrast: SectionMusicalContrast?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.structure_rhythm_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
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
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                )
                Text(
                    text = stringResource(R.string.structure_contrast_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = contrast.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (contrast.confidence == SectionRhythmConfidence.Low) {
                    Text(
                        text = stringResource(R.string.structure_warning_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.structure_contrast_low_confidence),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
