package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import kotlin.math.roundToLong

private val TouchWidth = 32.dp
private val LineWidth = 4.dp

@Composable
internal fun TimelineBoundaryHandle(
    leftSection: SongSection,
    rightSection: SongSection,
    durationMillis: Long,
    sectionAreaWidth: Dp,
    timelinePadding: Dp,
    onBoundaryChanged: (Int, Long) -> Unit,
) {
    val density = LocalDensity.current
    val areaWidthPx = with(density) { sectionAreaWidth.toPx() }.coerceAtLeast(1f)
    val minPosition = leftSection.startMillis + SongStructureFactory.MIN_SECTION_DURATION_MILLIS
    val maxPosition = rightSection.endMillis - SongStructureFactory.MIN_SECTION_DURATION_MILLIS
    var dragPosition by remember(leftSection.id, leftSection.endMillis, rightSection.id) {
        mutableStateOf<Long?>(null)
    }
    val displayedPosition = dragPosition ?: leftSection.endMillis
    val boundaryOffset = timelinePadding + sectionAreaWidth *
        (displayedPosition.toFloat() / durationMillis)
    val description = stringResource(
        R.string.structure_timeline_boundary_description,
        leftSection.toDisplayName(),
        rightSection.toDisplayName(),
    )

    Box(
        modifier = Modifier
            .offset(x = boundaryOffset - TouchWidth / 2)
            .width(TouchWidth)
            .fillMaxHeight()
            .testTag("structure_boundary_${leftSection.id}")
            .semantics { contentDescription = description }
            .pointerInput(leftSection.id, leftSection.endMillis, rightSection.id, durationMillis) {
                detectDragGestures(
                    onDragStart = { dragPosition = leftSection.endMillis },
                    onDragCancel = { dragPosition = null },
                    onDragEnd = {
                        val newPosition = dragPosition
                        dragPosition = null
                        if (newPosition != null && newPosition != leftSection.endMillis) {
                            onBoundaryChanged(leftSection.id, newPosition)
                        }
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        val delta = (amount.x / areaWidthPx * durationMillis).roundToLong()
                        dragPosition = ((dragPosition ?: leftSection.endMillis) + delta)
                            .coerceIn(minPosition, maxPosition)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 18.dp)
                .width(LineWidth)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                    RoundedCornerShape(50),
                ),
        )
        if (dragPosition != null) {
            Text(
                text = formatSectionTime(displayedPosition),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}
