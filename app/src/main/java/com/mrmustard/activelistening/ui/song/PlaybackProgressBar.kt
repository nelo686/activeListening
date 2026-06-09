package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun PlaybackProgressBar(
    positionMillis: Long,
    durationMillis: Long,
    onSeek: (Long) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val safeDuration = durationMillis.coerceAtLeast(1L)
    val progress = positionMillis.coerceIn(0L, safeDuration).toFloat() / safeDuration.toFloat()
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .pointerInput(enabled, safeDuration) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek((safeDuration * ratio).toLong())
                }
            },
    ) {
        val centerY = size.height / 2f
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 5f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = progressColor,
            start = Offset(0f, centerY),
            end = Offset(size.width * progress, centerY),
            strokeWidth = 5f,
            cap = StrokeCap.Round,
        )
    }
}
