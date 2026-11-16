package io.github.evenchen1948.radioil.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.evenchen1948.radioil.R
import io.github.evenchen1948.radioil.data.Station
import io.github.evenchen1948.radioil.data.StationCategory
import io.github.evenchen1948.radioil.player.PlaybackStatus
import io.github.evenchen1948.radioil.player.PlayerViewModel
import io.github.evenchen1948.radioil.ui.theme.RadioColors
import io.github.evenchen1948.radioil.ui.theme.SerifFamily
import java.time.LocalTime

private val ScreenPadding = 20.dp
private val CardWidth = 150.dp
private val CardSpacing = 12.dp

/** Order of the category chips and of the sections on the home screen. */
private val CategoryOrder = listOf(
    StationCategory.MUSIC,
    StationCategory.NEWS,
    StationCategory.TALK,
    StationCategory.REGIONAL,
    StationCategory.RELIGIOUS,
)

/**
 * Home screen: greeting, category chips, a carousel of station cards per
 * category (or a grid when one category is selected) and the floating player bar.
 */
@Composable
fun RadioScreen(viewModel: PlayerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf<StationCategory?>(null) }
    val isActive = state.status == PlaybackStatus.PLAYING || state.status == PlaybackStatus.LOADING
    val insets = WindowInsets.systemBars.asPaddingValues()

    // Back returns from a category to the full list; from the full list it leaves the app.
    BackHandler(enabled = filter != null) { filter = null }

    // The expanded player closes first: the handler registered last wins.
    var playerExpanded by rememberSaveable { mutableStateOf(false) }
    val showPlayer = playerExpanded && state.current != null
    BackHandler(enabled = showPlayer) { playerExpanded = false }

    // Previous/next step through the stations in the order the home screen lists them.
    val orderedStations = remember(state.stations) {
        CategoryOrder.flatMap { category -> state.stations.filter { it.category == category } }
    }

    fun playNeighbour(offset: Int) {
        val index = orderedStations.indexOf(state.current)
        if (index >= 0) viewModel.play(orderedStations[(index + offset).mod(orderedStations.size)])
    }

    @Composable
    fun Card(station: Station, modifier: Modifier) {
        val isCurrent = station == state.current
        val category = stringResource(station.category.labelRes())
        StationCard(
            station = station,
            subtitle = if (isCurrent) {
                stringResource(R.string.card_subtitle_current, category, stringResource(state.status.labelRes()))
            } else {
                category
            },
            isCurrent = isCurrent,
            isPlaying = isCurrent && state.status == PlaybackStatus.PLAYING,
            onClick = { viewModel.play(station) },
            modifier = modifier,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RadioColors.Background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Space for the player bar is always reserved, so the list never shifts when it appears.
            contentPadding = PaddingValues(
                top = insets.calculateTopPadding() + 28.dp,
                bottom = insets.calculateBottomPadding() + PlayerBarHeight + 32.dp,
            ),
        ) {
            item(key = "greeting") {
                Text(
                    text = stringResource(greetingRes()),
                    color = RadioColors.Text,
                    fontSize = 32.sp,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = ScreenPadding)
                        .semantics { heading() },
                )
            }

            item(key = "chips") {
                CategoryChips(
                    selected = filter,
                    onSelect = { filter = it },
                    modifier = Modifier.padding(top = 20.dp),
                )
            }

            val selectedCategory = filter
            if (selectedCategory == null) {
                CategoryOrder.forEach { category ->
                    val group = state.stations.filter { it.category == category }
                    if (group.isEmpty()) return@forEach
                    item(key = "section-$category") {
                        SectionHeader(
                            title = stringResource(category.labelRes()),
                            onShowAll = { filter = category },
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = ScreenPadding),
                            horizontalArrangement = Arrangement.spacedBy(CardSpacing),
                        ) {
                            items(group, key = { it.id }) { station ->
                                Card(station, Modifier.width(CardWidth))
                            }
                        }
                    }
                }
            } else {
                item(key = "section-$selectedCategory") {
                    SectionHeader(title = stringResource(selectedCategory.labelRes()), onShowAll = null)
                }
                val rows = state.stations.filter { it.category == selectedCategory }.chunked(2)
                items(rows, key = { row -> row.first().id }) { row ->
                    Row(
                        modifier = Modifier
                            .padding(horizontal = ScreenPadding)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(CardSpacing),
                    ) {
                        row.forEach { station -> Card(station, Modifier.weight(1f)) }
                        // Keep a lone last card at half width.
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            item(key = "privacy") { PrivacyPolicyLink() }
        }

        // The bar keeps showing the last station while it slides away.
        var lastStation by remember { mutableStateOf(state.current) }
        state.current?.let { lastStation = it }

        AnimatedVisibility(
            visible = state.current != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = insets.calculateBottomPadding() + 12.dp,
                )
                .widthIn(max = 560.dp),
            enter = slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            lastStation?.let { station ->
                PlayerBar(
                    station = station,
                    statusText = stringResource(state.status.barLabelRes()),
                    isError = state.status == PlaybackStatus.ERROR,
                    isActive = isActive,
                    playPauseLabel = playPauseLabel(state.status),
                    expandLabel = stringResource(R.string.action_open_player),
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onExpand = { playerExpanded = true },
                )
            }
        }

        AnimatedVisibility(
            visible = showPlayer,
            enter = slideInVertically(tween(250)) { it } + fadeIn(tween(150)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200)),
        ) {
            lastStation?.let { station ->
                PlayerScreen(
                    station = station,
                    categoryText = stringResource(station.category.labelRes()),
                    statusText = stringResource(state.status.barLabelRes()),
                    isPlaying = state.status == PlaybackStatus.PLAYING,
                    isError = state.status == PlaybackStatus.ERROR,
                    isActive = isActive,
                    playPauseLabel = playPauseLabel(state.status),
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onPrevious = { playNeighbour(-1) },
                    onNext = { playNeighbour(1) },
                    onCollapse = { playerExpanded = false },
                )
            }
        }
    }
}

@Composable
private fun playPauseLabel(status: PlaybackStatus): String = stringResource(
    when (status) {
        PlaybackStatus.PLAYING, PlaybackStatus.LOADING -> R.string.action_pause
        PlaybackStatus.ERROR -> R.string.action_retry
        else -> R.string.action_play
    },
)

/** Horizontally scrolling filter pills: "all" followed by one per category. */
@Composable
private fun CategoryChips(
    selected: StationCategory?,
    onSelect: (StationCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            Chip(label = stringResource(R.string.filter_all), selected = selected == null, onClick = { onSelect(null) })
        }
        items(CategoryOrder, key = { it.name }) { category ->
            Chip(
                label = stringResource(category.labelRes()),
                selected = selected == category,
                onClick = { onSelect(category) },
            )
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .background(if (selected) RadioColors.Gold else RadioColors.Surface)
            .border(1.dp, if (selected) RadioColors.Gold else RadioColors.Outline, CircleShape)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) RadioColors.OnGold else RadioColors.Text,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}

/** Section title with an optional "show all" link at the far end. */
@Composable
private fun SectionHeader(title: String, onShowAll: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ScreenPadding, end = 8.dp, top = 24.dp, bottom = 4.dp)
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = RadioColors.Text,
            fontSize = 20.sp,
            fontFamily = SerifFamily,
                    fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (onShowAll != null) {
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onShowAll)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.action_show_all),
                    color = RadioColors.Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

/** Footer link that opens the privacy policy in the browser, as Google Play requires. */
@Composable
private fun PrivacyPolicyLink() {
    val uriHandler = LocalUriHandler.current
    val url = stringResource(R.string.privacy_policy_url)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button) {
                    // No browser installed: nothing to open, and nothing worth crashing over.
                    runCatching { uriHandler.openUri(url) }
                }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.action_privacy_policy),
                color = RadioColors.Gold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun greetingRes(now: LocalTime = LocalTime.now()): Int = when (now.hour) {
    in 5..11 -> R.string.greeting_morning
    in 12..16 -> R.string.greeting_afternoon
    in 17..21 -> R.string.greeting_evening
    else -> R.string.greeting_night
}

private fun StationCategory.labelRes(): Int = when (this) {
    StationCategory.NEWS -> R.string.category_news
    StationCategory.MUSIC -> R.string.category_music
    StationCategory.TALK -> R.string.category_talk
    StationCategory.REGIONAL -> R.string.category_regional
    StationCategory.RELIGIOUS -> R.string.category_religious
}

/** Status as shown next to the category under the current station's card. */
private fun PlaybackStatus.labelRes(): Int = when (this) {
    PlaybackStatus.IDLE, PlaybackStatus.PAUSED -> R.string.status_paused
    PlaybackStatus.LOADING -> R.string.status_loading
    PlaybackStatus.PLAYING -> R.string.status_playing
    PlaybackStatus.ERROR -> R.string.status_error_short
}

/** Status as shown in the player bar. */
private fun PlaybackStatus.barLabelRes(): Int = when (this) {
    PlaybackStatus.IDLE, PlaybackStatus.PAUSED -> R.string.status_paused
    PlaybackStatus.LOADING -> R.string.status_loading
    PlaybackStatus.PLAYING -> R.string.status_live
    PlaybackStatus.ERROR -> R.string.status_error
}
