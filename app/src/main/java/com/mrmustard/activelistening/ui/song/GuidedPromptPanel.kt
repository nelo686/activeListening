package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.ui.song.structure.formatSectionTime
import com.mrmustard.activelistening.ui.song.structure.toDisplayName

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun GuidedPromptPanel(
    section: SongSection?,
    isGuidanceLoading: Boolean,
    guidanceError: GuidanceError?,
    guidanceIntensity: GuidanceIntensity,
    onConfirm: () -> Unit,
    onMarkUncertain: () -> Unit,
    onRepeat: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusMessage = when {
        isGuidanceLoading -> stringResource(R.string.guidance_status_loading)
        guidanceError == GuidanceError.MissingApiKey -> stringResource(R.string.guidance_status_missing_api_key)
        guidanceError == GuidanceError.UnableToGenerate -> stringResource(R.string.guidance_status_unable_to_generate)
        else -> stringResource(R.string.guidance_status_ready)
    }
    val statusColor = when (guidanceError) {
        null -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.guided_prompt_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
        )

        if (section == null) {
            Text(
                text = stringResource(R.string.guided_prompt_waiting),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        Text(
            text = stringResource(
                R.string.guided_prompt_section_context,
                section.toDisplayName(),
                formatSectionTime(section.startMillis),
                formatSectionTime(section.endMillis),
            ),
            style = MaterialTheme.typography.labelLarge,
            color = statusAccentColor(section.status),
        )
        if (guidanceIntensity == GuidanceIntensity.Normal) {
            Text(
                text = section.prompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.guided_actions_title),
                style = MaterialTheme.typography.labelLarge,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onConfirm) { Text(stringResource(R.string.guided_action_confirm)) }
                OutlinedButton(onClick = onMarkUncertain) { Text(stringResource(R.string.guided_action_uncertain)) }
                OutlinedButton(onClick = onRepeat) { Text(stringResource(R.string.guided_action_repeat)) }
                OutlinedButton(onClick = onSkip) { Text(stringResource(R.string.guided_action_skip)) }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.guided_action_confirm))
                }
                OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.guided_action_skip))
                }
            }
        }
    }
}

@Composable
private fun statusAccentColor(status: SectionStatus): Color =
    when (status) {
        SectionStatus.Suggested -> MaterialTheme.colorScheme.primary
        SectionStatus.Confirmed -> MaterialTheme.colorScheme.tertiary
        SectionStatus.Uncertain -> MaterialTheme.colorScheme.error
    }
