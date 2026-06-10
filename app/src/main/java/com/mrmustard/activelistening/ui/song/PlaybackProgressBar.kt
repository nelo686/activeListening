package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.min

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(enabled, safeDuration) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onSeek((safeDuration * (down.position.x / size.width).coerceIn(0f, 1f)).toLong())
                    drag(down.id) { change ->
                        val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek((safeDuration * ratio).toLong())
                    }
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Barra de progreso. Arrastra para avanzar o retroceder."
                    progressBarRangeInfo = androidx.compose.ui.semantics.ProgressBarRangeInfo(
                        current = progress,
                        range = 0f..1f,
                    )
                },
        ) {
            val centerY = size.height / 2f
            val thumbCenterX = (size.width * progress).coerceIn(0f, size.width)
            val thumbRadiusPx = min(size.height * 0.22f, 8.dp.toPx())

            drawLine(
                color = trackColor,
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 4f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = progressColor,
                start = Offset(0f, centerY),
                end = Offset(thumbCenterX, centerY),
                strokeWidth = 4f,
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = progressColor.copy(alpha = 0.16f),
                radius = thumbRadiusPx + 8f,
                center = Offset(thumbCenterX, centerY),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.86f),
                radius = thumbRadiusPx + 1.5f,
                center = Offset(thumbCenterX, centerY),
            )
            drawCircle(
                color = progressColor,
                radius = thumbRadiusPx,
                center = Offset(thumbCenterX, centerY),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.30f),
                radius = thumbRadiusPx - 2f,
                center = Offset(thumbCenterX, centerY),
                style = Stroke(width = 1.2f),
            )
        }
    }
}
