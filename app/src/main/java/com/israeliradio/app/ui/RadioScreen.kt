package com.israeliradio.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.israeliradio.app.R
import com.israeliradio.app.player.PlaybackStatus
import com.israeliradio.app.player.PlayerViewModel

/** Bare, unstyled screen for step 1: station buttons, current station, status, play/pause. */
@Composable
fun RadioScreen(viewModel: PlayerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isActive = state.status == PlaybackStatus.PLAYING || state.status == PlaybackStatus.LOADING

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Text(text = stringResource(R.string.app_name))

        state.stations.forEach { station ->
            Button(onClick = { viewModel.play(station) }) {
                Text(text = station.name)
            }
        }

        Text(text = state.current?.name ?: stringResource(R.string.no_station_selected))
        Text(text = stringResource(state.status.labelRes()))

        Button(onClick = viewModel::togglePlayPause, enabled = state.current != null) {
            Text(text = stringResource(if (isActive) R.string.action_pause else R.string.action_play))
        }
    }
}

private fun PlaybackStatus.labelRes(): Int = when (this) {
    PlaybackStatus.IDLE -> R.string.status_idle
    PlaybackStatus.LOADING -> R.string.status_loading
    PlaybackStatus.PLAYING -> R.string.status_playing
    PlaybackStatus.PAUSED -> R.string.status_paused
    PlaybackStatus.ERROR -> R.string.status_error
}
