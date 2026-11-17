package io.github.evenchen1948.radioil.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.github.evenchen1948.radioil.R
import io.github.evenchen1948.radioil.data.Station
import io.github.evenchen1948.radioil.ui.theme.RadioColors
import io.github.evenchen1948.radioil.ui.theme.SerifFamily
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToInt

/**
 * Full-screen player from design/2.png: status, the station as a large disc
 * tile, its name and category, the transport controls and a volume slider.
 * It only shows state and forwards commands; playback stays in the service.
 */
@Composable
fun PlayerScreen(
    station: Station,
    categoryText: String,
    statusText: String,
    isPlaying: Boolean,
    isError: Boolean,
    isActive: Boolean,
    isFavorite: Boolean,
    playPauseLabel: String,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RadioColors.Background)
            // Swallow taps so they never reach the home screen underneath.
            .pointerInput(Unit) { detectTapGestures { } }
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OutlinedRoundButton(
                label = stringResource(R.string.action_collapse),
                onClick = onCollapse,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                // Chevron pointing down.
                val w = size.width * 0.22f
                val h = size.height * 0.11f
                drawPath(
                    path = Path().apply {
                        moveTo(center.x - w / 2, center.y - h / 2)
                        lineTo(center.x, center.y + h / 2)
                        lineTo(center.x + w / 2, center.y - h / 2)
                    },
                    color = RadioColors.Text,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
            StatusLabel(text = statusText, isPlaying = isPlaying, isError = isError)
            OutlinedRoundButton(
                label = favoriteLabel(isFavorite),
                onClick = onToggleFavorite,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                drawStar(filled = isFavorite, radius = size.minDimension * 0.2f)
            }
        }

        // The disc takes whatever height is left, so short screens shrink it first.
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .aspectRatio(1f)
                    .border(1.dp, RadioColors.Gold, CircleShape)
                    .padding(16.dp)
                    .background(RadioColors.Surface, CircleShape)
                    .border(1.dp, RadioColors.Outline, CircleShape)
                    .padding(36.dp),
                contentAlignment = Alignment.Center,
            ) {
                StationTileLabel(
                    station = station,
                    mainColor = RadioColors.Gold,
                    maxMainSize = 96.sp,
                    captionSize = 15.sp,
                    captionGap = 4.dp,
                )
            }
        }

        Text(
            text = station.name,
            color = RadioColors.Text,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            fontFamily = SerifFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = categoryText,
            color = RadioColors.SecondaryText,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(Modifier.height(40.dp))

        // Transport controls keep the same order in every locale: previous on the left.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkipButton(label = stringResource(R.string.action_previous), forward = false, onClick = onPrevious)
                PlayPauseButton(
                    isActive = isActive,
                    label = playPauseLabel,
                    onClick = onTogglePlayPause,
                    size = 84.dp,
                )
                SkipButton(label = stringResource(R.string.action_next), forward = true, onClick = onNext)
            }
        }

        Spacer(Modifier.height(36.dp))

        VolumeRow()
    }
}

/** Letter-spaced status in the top bar; gold with a dot while the station is live. */
@Composable
private fun StatusLabel(text: String, isPlaying: Boolean, isError: Boolean) {
    val color = when {
        isError -> RadioColors.Error
        isPlaying -> RadioColors.Gold
        else -> RadioColors.SecondaryText
    }
    Row(
        // Leaves room for the round buttons at both edges.
        modifier = Modifier
            .padding(horizontal = 60.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isPlaying) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(color, CircleShape),
            )
        }
        Text(
            text = text,
            color = color,
            fontSize = 14.sp,
            // Long messages (the error text) stay readable without wide tracking.
            letterSpacing = if (isError) 0.sp else 3.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

/** A 48dp circle with a hairline outline, an optional [fill] and a glyph drawn in [draw]. */
@Composable
internal fun OutlinedRoundButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    draw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit,
) {
    Canvas(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(fill)
            .border(1.dp, RadioColors.Outline, CircleShape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        onDraw = draw,
    )
}

/** Outlined triangle with a bar: next station when [forward], previous otherwise. */
@Composable
private fun SkipButton(label: String, forward: Boolean, onClick: () -> Unit) {
    Canvas(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
    ) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val h = size.height * 0.3f
        val w = h * 0.85f
        // Drawn for "next" and mirrored around the centre for "previous".
        val dir = if (forward) 1f else -1f
        val back = center.x - dir * w / 2
        val tip = center.x + dir * w / 2
        drawPath(
            path = Path().apply {
                moveTo(back, center.y - h / 2)
                lineTo(tip - dir * 2.dp.toPx(), center.y)
                lineTo(back, center.y + h / 2)
                close()
            },
            color = RadioColors.Text,
            style = stroke,
        )
        val barX = tip + dir * 2.dp.toPx()
        drawLine(
            color = RadioColors.Text,
            start = Offset(barX, center.y - h / 2),
            end = Offset(barX, center.y + h / 2),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
    }
}

private const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"

/**
 * Slider for the system media volume, the same volume the hardware keys change,
 * so it stays in step with them and with the notification and headset controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolumeRow() {
    val context = LocalContext.current
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxLevel = remember(audioManager) { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    val minLevel = remember(audioManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } else {
            0
        }
    }
    val range = (maxLevel - minLevel).coerceAtLeast(1)

    fun currentFraction(): Float =
        (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - minLevel).toFloat() / range

    fun levelOf(fraction: Float): Int = minLevel + (fraction * range).roundToInt()

    var fraction by remember { mutableFloatStateOf(currentFraction()) }

    // Follow changes made elsewhere: hardware keys, the system volume panel, a headset.
    DisposableEffect(context, audioManager) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val actual = currentFraction()
                // Ignore the echo of our own change so the thumb does not snap while dragging.
                if (levelOf(fraction) != levelOf(actual)) fraction = actual
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(VOLUME_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        fraction = currentFraction()
        onDispose { context.unregisterReceiver(receiver) }
    }

    val label = stringResource(R.string.volume_label)
    val colors = SliderDefaults.colors(
        thumbColor = RadioColors.Gold,
        activeTrackColor = RadioColors.Gold,
        inactiveTrackColor = RadioColors.Track,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = label,
            color = RadioColors.SecondaryText,
            fontSize = 14.sp,
            // The slider announces the same label.
            modifier = Modifier.clearAndSetSemantics { },
        )
        Slider(
            value = fraction,
            onValueChange = { value ->
                fraction = value
                val level = levelOf(value)
                if (level != audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0)
                }
            },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = label },
            colors = colors,
            thumb = {
                Box(
                    Modifier
                        .size(20.dp)
                        .background(RadioColors.Gold, CircleShape),
                )
            },
            track = { state ->
                SliderDefaults.Track(
                    sliderState = state,
                    modifier = Modifier.height(6.dp),
                    colors = colors,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 0.dp,
                )
            },
        )
    }
}
