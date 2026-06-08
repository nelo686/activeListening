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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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

@Composable
private fun LearningPanel(
    content: SectionLearningContent?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.section_learning_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (content != null) {
            Text(
                text = content.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            content.uncertainNote?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Text(
                text = content.details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BoundaryEditor(
    title: String,
    suggestedTimeMillis: Long,
    onChange: (Long) -> Unit,
) {
    var text by remember(suggestedTimeMillis, title) {
        mutableStateOf(formatSectionTime(suggestedTimeMillis))
    }

    LaunchedEffect(suggestedTimeMillis) {
        text = formatSectionTime(suggestedTimeMillis)
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        val parsedTime = parseSectionTime(text)
        val hasEditedValue = text != formatSectionTime(suggestedTimeMillis)
        val isInvalid = text.isNotBlank() && parsedTime == null
        OutlinedTextField(
            value = text,
            onValueChange = { newValue ->
                text = newValue
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isInvalid,
            label = { Text(stringResource(R.string.structure_time_input_label)) },
            supportingText = {
                Text(
                    stringResource(
                        if (isInvalid) {
                            R.string.structure_time_input_error
                        } else {
                            R.string.structure_time_input_hint
                        },
                    ),
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        parsedTime?.let(onChange)
                    },
                    enabled = hasEditedValue && parsedTime != null,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_24),
                        contentDescription = stringResource(R.string.structure_time_apply),
                    )
                }
            },
        )
    }
}

private fun parseSectionTime(input: String): Long? {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return null

    val parts = trimmed.split(":")
    if (parts.size != 2) return null

    val minutes = parts[0].toLongOrNull() ?: return null
    val seconds = parts[1].toLongOrNull() ?: return null
    if (minutes < 0L || seconds !in 0L..59L) return null
    return (minutes * 60_000L) + (seconds * 1_000L)
}
