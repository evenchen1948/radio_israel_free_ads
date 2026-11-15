package io.github.evenchen1948.radioil.playback

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

/**
 * Makes "play" behave like a radio rather than a recording: resuming after a
 * pause jumps back to the live broadcast instead of continuing from where the
 * buffer stopped, and playing after an error reconnects.
 */
@OptIn(UnstableApi::class)
class LiveRadioPlayer(player: Player) : ForwardingPlayer(player) {

    /** True once playback has been paused, meaning buffered audio is behind live. */
    private var behindLive = false

    init {
        player.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (!playWhenReady) behindLive = true
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                behindLive = false
            }

            override fun onPlayerError(error: PlaybackException) {
                // An HLS stream that was paused or stalled long enough falls out of the
                // broadcaster's live window. That is not a real failure: rejoin at the live edge.
                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    player.seekToDefaultPosition()
                    player.prepare()
                }
            }
        })
    }

    override fun play() {
        if (playbackState == Player.STATE_IDLE) {
            prepare()
        } else if (behindLive) {
            seekToDefaultPosition()
        }
        behindLive = false
        super.play()
    }
}
