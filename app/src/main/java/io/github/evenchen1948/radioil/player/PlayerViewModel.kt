package io.github.evenchen1948.radioil.player

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import io.github.evenchen1948.radioil.data.FavoritesBackground
import io.github.evenchen1948.radioil.data.FavoritesRepository
import io.github.evenchen1948.radioil.data.SettingsRepository
import io.github.evenchen1948.radioil.data.Station
import io.github.evenchen1948.radioil.data.StationRepository
import io.github.evenchen1948.radioil.playback.PlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlaybackStatus { IDLE, LOADING, PLAYING, PAUSED, ERROR }

data class PlayerUiState(
    val stations: List<Station> = emptyList(),
    val current: Station? = null,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    /** Ids of the starred stations; null until they have been read from storage. */
    val favoriteIds: Set<String>? = null,
    /** The artwork behind the favorites tab; null until the choice has been read from storage. */
    val favoritesBackground: FavoritesBackground? = null,
    /** Whether the artwork is shown under the lighter scrim. */
    val favoritesBackgroundBright: Boolean = false,
)

/**
 * UI-side view of playback. The player itself lives in [PlaybackService]; this
 * class only sends commands to it through a [MediaController] and mirrors its
 * state, so the screen shows the truth even when playback was changed from the
 * notification, the lock screen or a headset.
 */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val stations = StationRepository(application).loadStations()
    private val stationsById = stations.associateBy { it.id }
    private val favorites = FavoritesRepository(application)
    private val settings = SettingsRepository(application)

    private val _uiState = MutableStateFlow(PlayerUiState(stations = stations))
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val controllerFuture = MediaController.Builder(
        application,
        SessionToken(application, ComponentName(application, PlaybackService::class.java)),
    ).buildAsync()

    private var controller: MediaController? = null

    /** A station tapped before the service connection was ready. */
    private var pendingStation: Station? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = syncFrom(player)
    }

    init {
        viewModelScope.launch {
            favorites.favoriteIds.collect { ids -> _uiState.update { it.copy(favoriteIds = ids) } }
        }
        viewModelScope.launch {
            settings.favoritesBackground.collect { background ->
                _uiState.update { it.copy(favoritesBackground = background) }
            }
        }
        viewModelScope.launch {
            settings.favoritesBackgroundBright.collect { bright ->
                _uiState.update { it.copy(favoritesBackgroundBright = bright) }
            }
        }
        controllerFuture.addListener(
            {
                val connected = runCatching { controllerFuture.get() }.getOrNull()
                if (connected == null) {
                    if (!controllerFuture.isCancelled) {
                        _uiState.update { it.copy(status = PlaybackStatus.ERROR) }
                    }
                } else {
                    controller = connected
                    connected.addListener(listener)
                    val queued = pendingStation
                    pendingStation = null
                    if (queued != null) play(queued) else syncFrom(connected)
                }
            },
            ContextCompat.getMainExecutor(application),
        )
    }

    fun play(station: Station) {
        val controller = controller
        if (controller == null) {
            pendingStation = station
            _uiState.update { it.copy(current = station, status = PlaybackStatus.LOADING) }
            return
        }
        // Only the id is sent; the service resolves it to the stream.
        // setMediaItem replaces whatever was playing before.
        controller.setMediaItem(MediaItem.Builder().setMediaId(station.id).build())
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val controller = controller ?: return
        when (_uiState.value.status) {
            PlaybackStatus.PLAYING, PlaybackStatus.LOADING -> controller.pause()
            // The service reconnects to the live stream on play, including after an error.
            else -> controller.play()
        }
    }

    fun setFavoritesBackground(background: FavoritesBackground) {
        viewModelScope.launch { settings.setFavoritesBackground(background) }
    }

    fun setFavoritesBackgroundBright(bright: Boolean) {
        viewModelScope.launch { settings.setFavoritesBackgroundBright(bright) }
    }

    fun toggleFavorite(station: Station) {
        viewModelScope.launch { favorites.toggle(station.id) }
    }

    private fun syncFrom(player: Player) {
        val current = player.currentMediaItem?.mediaId?.let(stationsById::get)
        val status = when {
            current == null -> PlaybackStatus.IDLE
            player.playerError != null -> PlaybackStatus.ERROR
            player.isPlaying -> PlaybackStatus.PLAYING
            player.playWhenReady && player.playbackState != Player.STATE_READY -> PlaybackStatus.LOADING
            // Ready but silent: paused by the user or by another app holding audio focus.
            else -> PlaybackStatus.PAUSED
        }
        _uiState.update { it.copy(current = current, status = status) }
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        // Releases only this connection; the service keeps playing.
        MediaController.releaseFuture(controllerFuture)
    }
}
