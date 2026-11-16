package io.github.evenchen1948.radioil.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.github.evenchen1948.radioil.MainActivity
import io.github.evenchen1948.radioil.R
import io.github.evenchen1948.radioil.data.Station
import io.github.evenchen1948.radioil.data.StationRepository
import io.github.evenchen1948.radioil.data.StreamType

/**
 * Owns the app's only player. Running it in a foreground media service keeps
 * audio alive with the screen off, and the media session gives the system the
 * notification, lock screen controls and headset buttons for free.
 *
 * The station list is exposed as a flat browsable library so that Android Auto
 * can be added later without restructuring.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaLibraryService() {

    private lateinit var session: MediaLibrarySession
    private lateinit var stationsById: Map<String, Station>

    override fun onCreate() {
        super.onCreate()
        stationsById = StationRepository(this).loadStations().associateBy { it.id }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        val player = ExoPlayer.Builder(this)
            // Audio focus: pause for calls, duck for navigation prompts.
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            // Pause when headphones are unplugged.
            .setHandleAudioBecomingNoisy(true)
            // Hold CPU and Wi-Fi awake while streaming with the screen off.
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaLibrarySession.Builder(this, LiveRadioPlayer(player), LibraryCallback())
            .setSessionActivity(openApp)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Closing the app from recents ("close all" or a swipe) ends the radio too:
        // stop the stream, drop the notification and let the service be destroyed.
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        session.player.release()
        session.release()
        super.onDestroy()
    }

    private fun Station.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setUri(streamUrl)
            // HLS is declared explicitly; MP3/AAC streams are detected from the data.
            .setMimeType(if (streamType == StreamType.HLS) MimeTypes.APPLICATION_M3U8 else null)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(name)
                    .setArtist(getString(R.string.app_name))
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_RADIO_STATION)
                    .build(),
            )
            .build()

    private inner class LibraryCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val root = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(getString(R.string.app_name))
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_RADIO_STATIONS)
                        .build(),
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(root, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val result = if (parentId == ROOT_ID) {
                LibraryResult.ofItemList(stationsById.values.map { it.toMediaItem() }, params)
            } else {
                LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            }
            return Futures.immediateFuture(result)
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val station = stationsById[mediaId]
            val result = if (station != null) {
                LibraryResult.ofItem(station.toMediaItem(), /* params = */ null)
            } else {
                LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            }
            return Futures.immediateFuture(result)
        }

        /**
         * Controllers only send a station id; the stream URL is resolved here so
         * stations.json stays the single source of station data.
         */
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> =
            Futures.immediateFuture(
                mediaItems.mapNotNull { stationsById[it.mediaId]?.toMediaItem() }.toMutableList(),
            )
    }

    private companion object {
        const val ROOT_ID = "root"
    }
}
