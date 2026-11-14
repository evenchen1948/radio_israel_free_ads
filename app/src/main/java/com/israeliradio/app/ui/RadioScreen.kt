package com.israeliradio.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.israeliradio.app.R
import com.israeliradio.app.data.Station
import com.israeliradio.app.data.StationCategory
import com.israeliradio.app.player.PlaybackStatus
import com.israeliradio.app.player.PlayerViewModel
import com.israeliradio.app.ui.theme.RadioColors

/** Home screen: the retro radio on top, now-playing text and station presets below. */
@Composable
fun RadioScreen(viewModel: PlayerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stations = state.stations
    val selectedIndex = stations.indexOf(state.current).takeIf { it >= 0 }
    val isActive = state.status == PlaybackStatus.PLAYING || state.status == PlaybackStatus.LOADING
    val currentName = state.current?.name ?: stringResource(R.string.no_station_selected)

    fun playNext() {
        if (stations.isEmpty()) return
        viewModel.play(stations[((selectedIndex ?: -1) + 1) % stations.size])
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RetroRadio(
            stationCount = stations.size,
            selectedIndex = selectedIndex,
            isActive = isActive,
            isLoading = state.status == PlaybackStatus.LOADING,
            playPauseLabel = stringResource(if (isActive) R.string.action_pause else R.string.action_play),
            nextStationLabel = stringResource(R.string.action_next_station),
            tunerLabel = stringResource(R.string.tuner_label),
            tunerState = currentName,
            onTogglePlayPause = {
                // With nothing tuned yet, the power controls start the first station.
                if (state.current == null) playNext() else viewModel.togglePlayPause()
            },
            onSelectIndex = { index -> stations.getOrNull(index)?.let(viewModel::play) },
            onNextStation = ::playNext,
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = currentName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(state.status.labelRes()),
            style = MaterialTheme.typography.bodyLarge,
            color = if (state.status == PlaybackStatus.PLAYING) RadioColors.Gold else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 4.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
        if (state.status == PlaybackStatus.ERROR) {
            TextButton(onClick = viewModel::togglePlayPause) {
                Text(text = stringResource(R.string.action_retry), color = RadioColors.Gold)
            }
        }

        Spacer(Modifier.height(28.dp))

        StationPresets(
            stations = stations,
            current = state.current,
            onSelect = viewModel::play,
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
        )
    }
}

/** Home screen order of the station groups. */
private val CategoryOrder = listOf(
    StationCategory.NEWS,
    StationCategory.MUSIC,
    StationCategory.TALK,
    StationCategory.REGIONAL,
    StationCategory.RELIGIOUS,
)

/** Station buttons styled as the radio's presets, grouped by category, two per row. */
@Composable
private fun StationPresets(
    stations: List<Station>,
    current: Station?,
    onSelect: (Station) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CategoryOrder.forEach { category ->
            val group = stations.filter { it.category == category }
            if (group.isEmpty()) return@forEach

            Text(
                text = stringResource(category.labelRes()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RadioColors.Gold,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .semantics { heading() },
            )
            group.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { station ->
                        PresetButton(
                            name = station.name,
                            selected = station == current,
                            onClick = { onSelect(station) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Keep a lone last button at half width.
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PresetButton(name: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        selected = selected,
        modifier = modifier.heightIn(min = 56.dp),
        shape = CircleShape,
        color = if (selected) RadioColors.Body else RadioColors.Cream,
        contentColor = RadioColors.Brown,
        border = BorderStroke(2.dp, RadioColors.Gold),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun StationCategory.labelRes(): Int = when (this) {
    StationCategory.NEWS -> R.string.category_news
    StationCategory.MUSIC -> R.string.category_music
    StationCategory.TALK -> R.string.category_talk
    StationCategory.REGIONAL -> R.string.category_regional
    StationCategory.RELIGIOUS -> R.string.category_religious
}

private fun PlaybackStatus.labelRes(): Int = when (this) {
    PlaybackStatus.IDLE -> R.string.status_idle
    PlaybackStatus.LOADING -> R.string.status_loading
    PlaybackStatus.PLAYING -> R.string.status_playing
    PlaybackStatus.PAUSED -> R.string.status_paused
    PlaybackStatus.ERROR -> R.string.status_error
}
