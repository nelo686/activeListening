package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection

@Composable
fun StructureTimeline(
    sections: List<SongSection>,
    selectedSectionId: Int?,
    activeSectionId: Int?,
    positionMillis: Long,
    durationMillis: Long,
    onSectionClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeDuration = durationMillis.coerceAtLeast(1L)
    val progress = positionMillis.coerceIn(0L, safeDuration).toFloat() / safeDuration

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.structure_timeline_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val minSectionWidth = 72.dp
            val timelineWidth = this.maxWidth
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                sections.forEach { section ->
                    val proportionalWidth = timelineWidth *
                        (section.durationMillis.coerceAtLeast(1L).toFloat() / safeDuration.toFloat())
                    SectionBlock(
                        section = section,
                        isSelected = section.id == selectedSectionId,
                        isActive = section.id == activeSectionId,
                        onClick = { onSectionClick(section.id) },
                        modifier = Modifier.widthIn(min = minSectionWidth)
                            .width(proportionalWidth.coerceAtLeast(minSectionWidth)),
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(
                R.string.structure_timeline_position,
                formatSectionTime(positionMillis),
                formatSectionTime(durationMillis),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionBlock(
    section: SongSection,
    isSelected: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = section.containerColor(isSelected, isActive)
    val contentColor = section.contentColor(isSelected, isActive)
    val description = stringResource(
        R.string.structure_timeline_section_description,
        section.label.toDisplayName(),
        formatSectionTime(section.startMillis),
        formatSectionTime(section.endMillis),
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .semantics { contentDescription = description }
            .clickable(onClick = onClick),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = if (isSelected || isActive) 4.dp else 0.dp,
        shape = MaterialTheme.shapes.small,
    ) {
        Box {
            if (section.isApproximate) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outline),
                )
            }
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = section.label.toDisplayName(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatSectionTime(section.startMillis)}-${formatSectionTime(section.endMillis)}",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
                Text(
                    text = stringResource(
                        R.string.structure_section_duration,
                        formatSectionTime(section.durationMillis),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = section.status.toDisplayName(),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SongSection.containerColor(
    isSelected: Boolean,
    isActive: Boolean,
): Color =
    when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isActive -> MaterialTheme.colorScheme.secondaryContainer
        status == SectionStatus.Confirmed -> MaterialTheme.colorScheme.tertiaryContainer
        status == SectionStatus.Uncertain -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

@Composable
private fun SongSection.contentColor(
    isSelected: Boolean,
    isActive: Boolean,
): Color =
    when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isActive -> MaterialTheme.colorScheme.onSecondaryContainer
        status == SectionStatus.Confirmed -> MaterialTheme.colorScheme.onTertiaryContainer
        status == SectionStatus.Uncertain -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
