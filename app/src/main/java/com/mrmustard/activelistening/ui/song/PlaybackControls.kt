package com.mrmustard.activelistening.ui.song

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.PlaybackError
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.time.formatTimeCode

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaybackControls(
    song: ImportedSong,
    playbackState: PlaybackState,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = playbackState.durationMillis.coerceAtLeast(0L)
    val position = playbackState.positionMillis.coerceIn(0L, duration.coerceAtLeast(1L))

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongArtwork(
                artwork = song.artwork,
                title = song.title,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = song.title,
                    modifier = Modifier.basicMarquee(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artist ?: stringResource(R.string.playback_unknown_artist),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PlaybackIconButton(
                isPlaying = playbackState.isPlaying,
                enabled = duration > 0L,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatTimeCode(position), style = MaterialTheme.typography.bodySmall)
            Text(formatTimeCode(duration), style = MaterialTheme.typography.bodySmall)
        }
        PlaybackProgressBar(
            positionMillis = position,
            durationMillis = duration,
            onSeek = onSeek,
            enabled = duration > 0L,
        )
        val playbackError = playbackState.error
        if (playbackError != null) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
private fun SongArtwork(
    artwork: ByteArray?,
    title: String,
) {
    val bitmap = remember(artwork) {
        artwork?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }
    val modifier = Modifier
        .size(55.dp)
        .clip(RoundedCornerShape(10.dp))

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = stringResource(R.string.playback_artwork_description, title),
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_music_note_24),
                contentDescription = stringResource(R.string.playback_artwork_description, title),
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
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
        modifier = modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
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
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun PlaybackError.toMessage(): String =
    when (this) {
        PlaybackError.UnableToPlay -> stringResource(R.string.playback_error_unable_to_play)
    }
