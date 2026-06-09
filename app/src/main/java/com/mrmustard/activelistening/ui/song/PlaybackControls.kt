package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.PlaybackError
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.time.formatTimeCode

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
            Text(formatTimeCode(position), style = MaterialTheme.typography.bodySmall)
            Text(formatTimeCode(duration), style = MaterialTheme.typography.bodySmall)
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
