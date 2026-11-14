package com.israeliradio.app.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.israeliradio.app.data.Station
import com.israeliradio.app.data.StationRepository
import com.israeliradio.app.data.StreamType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PlaybackStatus { IDLE, LOADING, PLAYING, PAUSED, ERROR }

data class PlayerUiState(
    val stations: List<Station> = emptyList(),
    val current: Station? = null,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
)

/**
 * Owns the app's single ExoPlayer for step 1 of the build order. Because there
 * is exactly one player, switching stations can never leave two streams playing.
 * Step 2 moves the player into the Media3 playback service.
 */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        PlayerUiState(stations = StationRepository(application).loadStations()),
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    private val player: ExoPlayer = ExoPlayer.Builder(application)
        .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
        .build()

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_BUFFERING && player.playWhenReady) {
                setStatus(PlaybackStatus.LOADING)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) setStatus(PlaybackStatus.PLAYING)
        }

        override fun onPlayerError(error: PlaybackException) {
            setStatus(PlaybackStatus.ERROR)
        }
    }

    init {
        player.addListener(listener)
    }

    fun play(station: Station) {
        _uiState.update { it.copy(current = station) }
        start(station)
    }

    fun togglePlayPause() {
        val state = _uiState.value
        val station = state.current ?: return
        when (state.status) {
            PlaybackStatus.PLAYING, PlaybackStatus.LOADING -> {
                // Drop the connection instead of buffering a live stream while paused,
                // so resuming rejoins the live broadcast.
                player.stop()
                setStatus(PlaybackStatus.PAUSED)
            }
            else -> start(station)
        }
    }

    private fun start(station: Station) {
        setStatus(PlaybackStatus.LOADING)
        val item = MediaItem.Builder()
            .setMediaId(station.id)
            .setUri(station.streamUrl)
            // HLS is declared explicitly; MP3/AAC streams are detected from the data.
            .setMimeType(if (station.streamType == StreamType.HLS) MimeTypes.APPLICATION_M3U8 else null)
            .build()
        // setMediaItem replaces whatever was playing before.
        player.setMediaItem(item)
        player.prepare()
        player.play()
    }

    private fun setStatus(status: PlaybackStatus) {
        _uiState.update { it.copy(status = status) }
    }

    override fun onCleared() {
        player.removeListener(listener)
        player.release()
    }
}
