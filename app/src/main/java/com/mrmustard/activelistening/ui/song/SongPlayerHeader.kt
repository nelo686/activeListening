package com.mrmustard.activelistening.ui.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.domain.PlaybackState

@Composable
fun SongPlayerHeader(
    title: String,
    playbackState: PlaybackState,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onChangeSongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlaybackControls(
                title = title,
                playbackState = playbackState,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
                onSeek = onSeek,
                onChangeSongClick = onChangeSongClick,
            )
        }
    }
}
