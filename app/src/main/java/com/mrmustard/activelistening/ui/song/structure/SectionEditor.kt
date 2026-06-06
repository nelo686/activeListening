package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.ui.song.GuidanceError

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectionEditor(
    section: SongSection?,
    isGuidanceLoading: Boolean,
    guidanceError: GuidanceError?,
    isGuidanceReduced: Boolean,
    onGuidanceReducedChange: () -> Unit,
    onLabelSelected: (SectionLabel) -> Unit,
    onConfirmClick: () -> Unit,
    onUncertainClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onAdjustStart: (Long) -> Unit,
    onAdjustEnd: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GuidanceStatus(
            isLoading = isGuidanceLoading,
            error = guidanceError,
        )

        if (section == null) {
            Text(
                text = stringResource(R.string.structure_section_waiting),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

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
                onClick = onUncertainClick,
                label = { Text(section.status.toDisplayName()) },
            )
        }

        if (!isGuidanceReduced) {
            Text(
                text = section.prompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.guidance_reduced_label),
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = isGuidanceReduced,
                onCheckedChange = { onGuidanceReducedChange() },
            )
        }

        HorizontalDivider()

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onConfirmClick,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.guided_action_confirm))
            }
            OutlinedButton(
                onClick = onUncertainClick,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.guided_action_uncertain))
            }
        }

        BoundaryControls(
            title = stringResource(R.string.structure_adjust_start),
            onAdjust = onAdjustStart,
        )
        BoundaryControls(
            title = stringResource(R.string.structure_adjust_end),
            onAdjust = onAdjustEnd,
        )

        OutlinedButton(
            onClick = onRepeatClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.structure_repeat_section))
        }
    }
}

@Composable
private fun GuidanceStatus(
    isLoading: Boolean,
    error: GuidanceError?,
) {
    val message = when {
        isLoading -> stringResource(R.string.guidance_status_loading)
        error == GuidanceError.MissingApiKey -> stringResource(R.string.guidance_status_missing_api_key)
        error == GuidanceError.UnableToGenerate -> stringResource(R.string.guidance_status_unable_to_generate)
        else -> stringResource(R.string.guidance_status_ready)
    }
    val color = if (error == null) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.error
    }

    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = color,
    )
}

@Composable
private fun BoundaryControls(
    title: String,
    onAdjust: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AdjustmentButton(label = "-5s", deltaMillis = -5_000L, onAdjust = onAdjust)
            AdjustmentButton(label = "-1s", deltaMillis = -1_000L, onAdjust = onAdjust)
            AdjustmentButton(label = "+1s", deltaMillis = 1_000L, onAdjust = onAdjust)
            AdjustmentButton(label = "+5s", deltaMillis = 5_000L, onAdjust = onAdjust)
        }
    }
}

@Composable
private fun RowScope.AdjustmentButton(
    label: String,
    deltaMillis: Long,
    onAdjust: (Long) -> Unit,
) {
    OutlinedButton(
        onClick = { onAdjust(deltaMillis) },
        modifier = Modifier.weight(1f),
    ) {
        Text(label)
    }
}
