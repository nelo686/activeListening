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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
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
            val progressEndX = (size.width * progress).coerceIn(0f, size.width)
            val strokeWidth = 8.dp.toPx()

            drawLine(
                color = trackColor,
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            if (progressEndX > 0f) {
                drawLine(
                    color = progressColor,
                    start = Offset(0f, centerY),
                    end = Offset(progressEndX, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
