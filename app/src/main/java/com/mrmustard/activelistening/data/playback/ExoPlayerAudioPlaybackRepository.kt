package com.mrmustard.activelistening.data.playback

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mrmustard.activelistening.domain.ImportedSong
import com.mrmustard.activelistening.domain.PlaybackState
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@ViewModelScoped
class ExoPlayerAudioPlaybackRepository @Inject constructor(
    @ApplicationContext context: Context,
) : AudioPlaybackRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val player = ExoPlayer.Builder(context).build()
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            publishState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            publishState()
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                errorMessage = "No se ha podido reproducir este archivo.",
            )
        }
    }

    init {
        player.addListener(listener)
    }

    override fun load(song: ImportedSong) {
        player.setMediaItem(MediaItem.fromUri(song.uri))
        player.prepare()
        _playbackState.value = PlaybackState(
            isReady = false,
            durationMillis = song.durationMillis,
        )
    }

    override fun play() {
        player.play()
        publishState()
    }

    override fun pause() {
        player.pause()
        publishState()
    }

    override fun seekTo(positionMillis: Long) {
        player.seekTo(positionMillis.coerceAtLeast(0L))
        publishState()
    }

    override fun release() {
        stopProgressUpdates()
        player.removeListener(listener)
        player.release()
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (isActive) {
                publishState()
                delay(PROGRESS_INTERVAL_MILLIS.milliseconds)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
        publishState()
    }

    private fun publishState() {
        val playerDuration = player.duration.takeUnless { it == C.TIME_UNSET } ?: 0L
        _playbackState.value = _playbackState.value.copy(
            isReady = player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_ENDED,
            isPlaying = player.isPlaying,
            positionMillis = player.currentPosition.coerceAtLeast(0L),
            durationMillis = playerDuration.takeIf { it > 0L } ?: _playbackState.value.durationMillis,
            errorMessage = null,
        )
    }

    private companion object {
        const val PROGRESS_INTERVAL_MILLIS = 500L
    }
}
