package com.mrmustard.activelistening.ui.importsong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R

@Composable
fun GuidedListeningPrompt(
    marker: GuidedListeningMarker?,
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

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
private fun GuidedListeningMarkerStatus.toLabel(): String =
    when (this) {
        GuidedListeningMarkerStatus.Pending -> stringResource(R.string.guided_status_pending)
        GuidedListeningMarkerStatus.Current -> stringResource(R.string.guided_status_current)
        GuidedListeningMarkerStatus.Reviewed -> stringResource(R.string.guided_status_reviewed)
        GuidedListeningMarkerStatus.Uncertain -> stringResource(R.string.guided_status_uncertain)
        GuidedListeningMarkerStatus.Skipped -> stringResource(R.string.guided_status_skipped)
    }
