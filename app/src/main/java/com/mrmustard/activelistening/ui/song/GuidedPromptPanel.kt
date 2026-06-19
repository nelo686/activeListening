package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.ui.song.structure.formatSectionTime
import com.mrmustard.activelistening.ui.song.structure.toDisplayName

private val GuidedPromptShape = RoundedCornerShape(24.dp)
private val GuidedPromptAccentWidth = 6.dp
private val PromptCardShape = RoundedCornerShape(20.dp)
private val ActionButtonShape = RoundedCornerShape(16.dp)

@Composable
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
    val statusMessage = guidanceStatusMessage(
        isGuidanceLoading = isGuidanceLoading,
        guidanceError = guidanceError,
    )
    val statusColor = guidanceStatusColor(guidanceError)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = GuidedPromptShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(GuidedPromptAccentWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Header(statusMessage = statusMessage, statusColor = statusColor)

                PromptCard(
                    section = section,
                    isGuidanceLoading = isGuidanceLoading,
                    guidanceIntensity = guidanceIntensity,
                )

                ActionArea(
                    guidanceIntensity = guidanceIntensity,
                    onConfirm = onConfirm,
                    onMarkUncertain = onMarkUncertain,
                    onRepeat = onRepeat,
                    onSkip = onSkip,
                )
            }
        }
    }
}

@Composable
private fun Header(
    statusMessage: String?,
    statusColor: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
            ) {
                Text(
                    text = stringResource(R.string.guided_prompt_badge),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = stringResource(R.string.guided_prompt_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (statusMessage != null) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
            )
        }
    }
}

@Composable
private fun PromptCard(
    section: SongSection?,
    isGuidanceLoading: Boolean,
    guidanceIntensity: GuidanceIntensity,
) {
    Surface(
        shape = PromptCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (section == null) {
                Text(
                    text = stringResource(R.string.guided_prompt_waiting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.guided_prompt_section_context,
                        section.toDisplayName(),
                        formatSectionTime(section.startMillis),
                        formatSectionTime(section.endMillis),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusAccentColor(section.status),
                )
                Text(
                    text = section.prompt,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (isGuidanceLoading) {
                    Text(
                        text = stringResource(R.string.guidance_status_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else if (guidanceIntensity == GuidanceIntensity.Reduced) {
                    Text(
                        text = stringResource(R.string.guidance_reduced_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionArea(
    guidanceIntensity: GuidanceIntensity,
    onConfirm: () -> Unit,
    onMarkUncertain: () -> Unit,
    onRepeat: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (guidanceIntensity) {
                GuidanceIntensity.Normal -> {
                    PrimaryActionButton(
                        text = stringResource(R.string.guided_action_uncertain),
                        onClick = onMarkUncertain,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                    SecondaryActionButton(
                        text = stringResource(R.string.guided_action_confirm),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }

                GuidanceIntensity.Reduced -> {
                    PrimaryActionButton(
                        text = stringResource(R.string.guided_action_confirm),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                    SecondaryActionButton(
                        text = stringResource(R.string.guided_action_skip),
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onRepeat,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = ActionButtonShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(
                text = stringResource(R.string.guided_action_repeat),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }

        if (guidanceIntensity == GuidanceIntensity.Normal) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = stringResource(R.string.guided_action_skip),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = ActionButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = ActionButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun guidanceStatusMessage(
    isGuidanceLoading: Boolean,
    guidanceError: GuidanceError?,
): String? =
    when {
        isGuidanceLoading -> stringResource(R.string.guidance_status_loading)
        guidanceError == GuidanceError.MissingApiKey -> stringResource(R.string.guidance_status_missing_api_key)
        guidanceError == GuidanceError.UnableToGenerate -> stringResource(R.string.guidance_status_unable_to_generate)
        else -> null
    }

@Composable
private fun guidanceStatusColor(guidanceError: GuidanceError?): Color =
    when (guidanceError) {
        null -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

@Composable
private fun statusAccentColor(status: SectionStatus): Color =
    when (status) {
        SectionStatus.Suggested -> MaterialTheme.colorScheme.primary
        SectionStatus.Confirmed -> MaterialTheme.colorScheme.tertiary
        SectionStatus.Uncertain -> MaterialTheme.colorScheme.error
    }
