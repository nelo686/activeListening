package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.PlaybackError
import com.mrmustard.activelistening.domain.PlaybackState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaybackControls(
    title: String,
    playbackState: PlaybackState,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onChangeSongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = playbackState.durationMillis.coerceAtLeast(0L)
    val position = playbackState.positionMillis.coerceIn(0L, duration.coerceAtLeast(1L))

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
            )
            PlaybackIconButton(
                isPlaying = playbackState.isPlaying,
                enabled = duration > 0L,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
            )
            IconButton(
                onClick = onChangeSongClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_music_note_24),
                    contentDescription = stringResource(R.string.import_song_change_song),
                )
            }
        }
        PlaybackProgressBar(
            positionMillis = position,
            durationMillis = duration,
            onSeek = onSeek,
            enabled = duration > 0L,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatTime(position), style = MaterialTheme.typography.bodySmall)
            Text(formatTime(duration), style = MaterialTheme.typography.bodySmall)
        }
        val playbackError = playbackState.error
        if (playbackError != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = playbackError.toMessage(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun PlaybackProgressBar(
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

@Composable
fun PlaybackIconButton(
    isPlaying: Boolean,
    enabled: Boolean,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = if (isPlaying) onPauseClick else onPlayClick,
        enabled = enabled,
        modifier = modifier.size(40.dp),
    ) {
        Icon(
            painter = painterResource(
                if (isPlaying) {
                    R.drawable.ic_pause_24
                } else {
                    R.drawable.ic_play_arrow_24
                },
            ),
            contentDescription = stringResource(
                if (isPlaying) {
                    R.string.playback_pause
                } else {
                    R.string.playback_play
                },
            ),
        )
    }
}

@Composable
private fun PlaybackError.toMessage(): String =
    when (this) {
        PlaybackError.UnableToPlay -> stringResource(R.string.playback_error_unable_to_play)
    }

private fun formatTime(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0L) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
