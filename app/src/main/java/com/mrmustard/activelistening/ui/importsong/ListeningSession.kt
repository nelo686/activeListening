package com.mrmustard.activelistening.ui.importsong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.PlaybackState

@Composable
fun ListeningSession(
    title: String,
    playbackState: PlaybackState,
    isGuidedSessionActive: Boolean,
    isGuidanceLoading: Boolean,
    guidanceError: GuidanceError?,
    guidedTimeline: List<GuidedListeningMarker>,
    currentGuidedMarker: GuidedListeningMarker?,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onStartGuidedSession: () -> Unit,
    onConfirmGuidedMarker: () -> Unit,
    onMarkGuidedMarkerUncertain: () -> Unit,
    onSkipGuidedMarker: () -> Unit,
    onRepeatGuidedMarker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.import_song_listening_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PlaybackControls(
                playbackState = playbackState,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
                onSeek = onSeek,
            )

            if (isGuidedSessionActive) {
                GuidedListeningTimeline(
                    markers = guidedTimeline,
                    currentMarker = currentGuidedMarker,
                    positionMillis = playbackState.positionMillis,
                    durationMillis = playbackState.durationMillis,
                )
                GuidedListeningPrompt(
                    marker = currentGuidedMarker,
                    isGuidanceLoading = isGuidanceLoading,
                    guidanceError = guidanceError,
                    onConfirmClick = onConfirmGuidedMarker,
                    onUncertainClick = onMarkGuidedMarkerUncertain,
                    onSkipClick = onSkipGuidedMarker,
                    onRepeatClick = onRepeatGuidedMarker,
                )
            } else {
                Button(
                    onClick = onStartGuidedSession,
                    enabled = playbackState.durationMillis > 0L,
                ) {
                    Text(stringResource(R.string.guided_listening_start))
                }
            }
        }
    }
}
