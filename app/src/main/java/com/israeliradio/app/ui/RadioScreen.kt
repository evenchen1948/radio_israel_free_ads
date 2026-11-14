package com.israeliradio.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
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

/** Screens shorter than this scroll the controls together with the station list. */
private val PinnedControlsMinHeight = 600.dp

/** Home screen: the tuner-style player on top and the station list below. */
@Composable
fun RadioScreen(viewModel: PlayerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stations = state.stations
    val selectedIndex = stations.indexOf(state.current).takeIf { it >= 0 }
    val isActive = state.status == PlaybackStatus.PLAYING || state.status == PlaybackStatus.LOADING
    val currentName = state.current?.name ?: stringResource(R.string.no_station_selected)

    fun playOffset(offset: Int) {
        if (stations.isEmpty()) return
        // With nothing tuned yet, "next" starts at the first station and "previous" at the last.
        val from = selectedIndex ?: if (offset > 0) -1 else 0
        viewModel.play(stations[(from + offset).mod(stations.size)])
    }

    val controls: @Composable () -> Unit = {
        Column {
            PlayerDisplay(
                stationName = currentName,
                statusLabel = stringResource(state.status.labelRes()),
                statusColor = when (state.status) {
                    PlaybackStatus.PLAYING -> RadioColors.Lime
                    PlaybackStatus.ERROR -> RadioColors.Indicator
                    else -> RadioColors.DisplayMuted
                },
                retryLabel = if (state.status == PlaybackStatus.ERROR) stringResource(R.string.action_retry) else null,
                onRetry = viewModel::togglePlayPause,
            )
            TunerScale(
                stationCount = stations.size,
                selectedIndex = selectedIndex,
                tunerLabel = stringResource(R.string.tuner_label),
                tunerState = currentName,
                nextStationLabel = stringResource(R.string.action_next_station),
                onSelectIndex = { index -> stations.getOrNull(index)?.let(viewModel::play) },
                onNextStation = { playOffset(1) },
            )
            TransportControls(
                isActive = isActive,
                playPauseLabel = stringResource(if (isActive) R.string.action_pause else R.string.action_play),
                previousLabel = stringResource(R.string.action_previous_station),
                nextLabel = stringResource(R.string.action_next_station),
                onTogglePlayPause = {
                    // With nothing tuned yet, play starts the first station.
                    if (state.current == null) playOffset(1) else viewModel.togglePlayPause()
                },
                onPrevious = { playOffset(-1) },
                onNext = { playOffset(1) },
            )
        }
    }
    val stationList: @Composable (Modifier) -> Unit = { modifier ->
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            StationPresets(
                stations = stations,
                current = state.current,
                onSelect = viewModel::play,
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(RadioColors.Surface),
    ) {
        if (maxHeight >= PinnedControlsMinHeight) {
            Column {
                controls()
                stationList(Modifier.weight(1f).verticalScroll(rememberScrollState()))
            }
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                controls()
                stationList(Modifier)
            }
        }
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

/** Station buttons grouped by category, two per row. */
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
                color = RadioColors.MutedText,
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
        color = if (selected) RadioColors.Slate else Color.White,
        contentColor = if (selected) Color.White else RadioColors.Icon,
        border = BorderStroke(1.dp, if (selected) RadioColors.Slate else RadioColors.PresetBorder),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
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
