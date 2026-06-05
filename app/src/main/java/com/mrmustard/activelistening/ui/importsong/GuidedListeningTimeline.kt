package com.mrmustard.activelistening.ui.importsong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R

@Composable
fun GuidedListeningTimeline(
    markers: List<GuidedListeningMarker>,
    currentMarker: GuidedListeningMarker?,
    positionMillis: Long,
    durationMillis: Long,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val pendingColor = MaterialTheme.colorScheme.outline
    val reviewedColor = MaterialTheme.colorScheme.tertiary
    val uncertainColor = MaterialTheme.colorScheme.secondary
    val skippedColor = MaterialTheme.colorScheme.error
    val currentColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.guided_timeline_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            val centerY = size.height / 2f
            val safeDuration = durationMillis.coerceAtLeast(1L)
            val progressX = (positionMillis.coerceIn(0L, safeDuration).toFloat() / safeDuration) *
                size.width

            drawLine(
                color = trackColor,
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 8f,
            )
            drawLine(
                color = progressColor,
                start = Offset(0f, centerY),
                end = Offset(progressX, centerY),
                strokeWidth = 8f,
            )

            markers.forEach { marker ->
                val x = (marker.positionMillis.coerceIn(0L, safeDuration).toFloat() / safeDuration) *
                    size.width
                val isCurrent = marker.id == currentMarker?.id
                val markerColor = marker.status.toColor(
                    isCurrent = isCurrent,
                    currentColor = currentColor,
                    pendingColor = pendingColor,
                    reviewedColor = reviewedColor,
                    uncertainColor = uncertainColor,
                    skippedColor = skippedColor,
                )
                drawCircle(
                    color = markerColor,
                    radius = if (isCurrent) 13f else 10f,
                    center = Offset(x, centerY),
                )
            }
        }
    }
}

private fun GuidedListeningMarkerStatus.toColor(
    isCurrent: Boolean,
    currentColor: Color,
    pendingColor: Color,
    reviewedColor: Color,
    uncertainColor: Color,
    skippedColor: Color,
): Color =
    when {
        isCurrent -> currentColor
        this == GuidedListeningMarkerStatus.Reviewed -> reviewedColor
        this == GuidedListeningMarkerStatus.Uncertain -> uncertainColor
        this == GuidedListeningMarkerStatus.Skipped -> skippedColor
        else -> pendingColor
    }

