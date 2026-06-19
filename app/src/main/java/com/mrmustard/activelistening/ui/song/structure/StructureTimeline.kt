package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection

private val TimelineHeight = 150.dp
private val TimelinePadding = 6.dp
private val SectionCornerRadius = 10.dp
private val PlaybackCursorColor = Color(0xFFC5222A)

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
            val contentWidth = maxOf(
                maxWidth,
                (safeDuration / 1_000f * 2.4f).dp,
            )
            val sectionAreaWidth = contentWidth - TimelinePadding * 2
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
            ) {
                Box(
                    modifier = Modifier
                        .width(contentWidth)
                        .height(TimelineHeight)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(18.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(18.dp),
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(TimelinePadding),
                    ) {
                        sections.forEach { section ->
                            val proportionalWidth = sectionAreaWidth *
                                (section.durationMillis.coerceAtLeast(1L).toFloat() / safeDuration)
                            SectionBlock(
                                section = section,
                                isSelected = section.id == selectedSectionId,
                                isActive = section.id == activeSectionId,
                                onClick = { onSectionClick(section.id) },
                                modifier = Modifier
                                    .width(proportionalWidth)
                                    .fillMaxHeight(),
                            )
                        }
                    }

                    PlaybackCursor(
                        progress = progress,
                        sectionAreaWidth = sectionAreaWidth,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaybackCursor(
    progress: Float,
    sectionAreaWidth: androidx.compose.ui.unit.Dp,
) {
    val cursorDescription = stringResource(
        R.string.structure_timeline_cursor_description,
        (progress * 100).toInt(),
    )

    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .width(sectionAreaWidth + TimelinePadding * 2)
            .semantics { contentDescription = cursorDescription },
    ) {
        val cursorX = TimelinePadding.toPx() + sectionAreaWidth.toPx() * progress
        val triangleHalfWidth = 9.dp.toPx()
        val triangleHeight = 10.dp.toPx()
        val lineWidth = 3.dp.toPx()
        val triangle = Path().apply {
            moveTo(cursorX - triangleHalfWidth, 0f)
            lineTo(cursorX + triangleHalfWidth, 0f)
            lineTo(cursorX, triangleHeight)
            close()
        }

        drawPath(path = triangle, color = PlaybackCursorColor)
        drawLine(
            color = PlaybackCursorColor,
            start = Offset(cursorX, triangleHeight),
            end = Offset(cursorX, size.height - TimelinePadding.toPx()),
            strokeWidth = lineWidth,
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
        section.toDisplayName(),
        formatSectionTime(section.startMillis),
        formatSectionTime(section.endMillis),
    )
    val shape = RoundedCornerShape(SectionCornerRadius)

    Surface(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 22.dp)
            .semantics { contentDescription = description }
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                },
            ),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = if (isSelected || isActive) 3.dp else 0.dp,
        shape = shape,
    ) {
        Box {
            if (section.status == SectionStatus.Suggested) {
                Text(
                    text = section.status.toDisplayName(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .border(
                            width = 1.dp,
                            color = contentColor.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(50),
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = section.toDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatSectionTime(section.startMillis)} - ${formatSectionTime(section.endMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
                Text(
                    text = formatDurationLabel(section.durationMillis),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

private fun formatDurationLabel(durationMillis: Long): String =
    "Duración ${formatSectionTime(durationMillis)}"

@Composable
private fun SongSection.containerColor(
    isSelected: Boolean,
    isActive: Boolean,
): Color =
    when {
        isSelected -> MaterialTheme.colorScheme.surface
        isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        status == SectionStatus.Confirmed -> MaterialTheme.colorScheme.tertiaryContainer
        status == SectionStatus.Uncertain -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }

@Composable
private fun SongSection.contentColor(
    isSelected: Boolean,
    isActive: Boolean,
): Color =
    when {
        isSelected -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.onPrimaryContainer
        status == SectionStatus.Confirmed -> MaterialTheme.colorScheme.onTertiaryContainer
        status == SectionStatus.Uncertain -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
