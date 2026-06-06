package com.mrmustard.activelistening.ui.song.guide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.ui.song.GuidanceError

@Composable
fun GuidedListeningPrompt(
    marker: GuidedListeningMarker?,
    isGuidanceLoading: Boolean,
    guidanceError: GuidanceError?,
    onConfirmClick: () -> Unit,
    onUncertainClick: () -> Unit,
    onSkipClick: () -> Unit,
    onRepeatClick: () -> Unit,
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

        if (marker == null) {
            Text(
                text = stringResource(R.string.guided_prompt_waiting),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.guided_prompt_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = marker.status.toLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = marker.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = marker.prompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.guided_actions_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onRepeatClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.guided_action_repeat))
                }
                OutlinedButton(
                    onClick = onSkipClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.guided_action_skip))
                }
            }
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
private fun GuidedListeningMarkerStatus.toLabel(): String =
    when (this) {
        GuidedListeningMarkerStatus.Pending -> stringResource(R.string.guided_status_pending)
        GuidedListeningMarkerStatus.Current -> stringResource(R.string.guided_status_current)
        GuidedListeningMarkerStatus.Reviewed -> stringResource(R.string.guided_status_reviewed)
        GuidedListeningMarkerStatus.Uncertain -> stringResource(R.string.guided_status_uncertain)
        GuidedListeningMarkerStatus.Skipped -> stringResource(R.string.guided_status_skipped)
    }
