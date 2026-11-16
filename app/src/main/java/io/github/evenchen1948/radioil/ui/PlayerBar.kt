package io.github.evenchen1948.radioil.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.evenchen1948.radioil.data.Station
import io.github.evenchen1948.radioil.ui.theme.RadioColors
import io.github.evenchen1948.radioil.ui.theme.SerifFamily

/** Height the bar occupies, used to reserve space under the list so nothing shifts when it appears. */
val PlayerBarHeight = 72.dp

private val BarShape = RoundedCornerShape(24.dp)

/**
 * Floating bar for the current station: small round tile, name, status and the
 * play/pause button. Tapping anywhere else on it opens the expanded player.
 */
@Composable
fun PlayerBar(
    station: Station,
    statusText: String,
    isError: Boolean,
    isActive: Boolean,
    playPauseLabel: String,
    expandLabel: String,
    onTogglePlayPause: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(BarShape)
            .background(RadioColors.Surface)
            .border(1.dp, RadioColors.Outline, BarShape)
            .clickable(role = Role.Button, onClickLabel = expandLabel, onClick = onExpand)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(RadioColors.Background, CircleShape)
                .border(1.dp, RadioColors.Gold, CircleShape)
                .clearAndSetSemantics { },
            contentAlignment = Alignment.Center,
        ) {
            // Too small for the full tile label: the number, or the first letter of the name.
            val label = station.tileLabel()
            Text(
                text = if (label.caption != null) label.main else label.main.trim().take(1),
                color = RadioColors.Gold,
                fontSize = if (label.main.length > 3) 13.sp else 17.sp,
                fontFamily = SerifFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = station.name,
                color = RadioColors.Text,
                fontSize = 18.sp,
                fontFamily = SerifFamily,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = statusText,
                color = if (isError) RadioColors.Error else RadioColors.SecondaryText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        // The largest control in the bar.
        PlayPauseButton(
            isActive = isActive,
            label = playPauseLabel,
            onClick = onTogglePlayPause,
            size = 52.dp,
        )
    }
}

/** Solid gold circle showing a pause glyph while active and a play glyph otherwise. */
@Composable
fun PlayPauseButton(
    isActive: Boolean,
    label: String,
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(RadioColors.Gold)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
    ) {
        if (isActive) {
            val barWidth = this.size.width * 0.07f
            val barHeight = this.size.height * 0.24f
            val gap = this.size.width * 0.075f
            for (side in listOf(-1f, 1f)) {
                val x = center.x + side * (gap + barWidth) / 2 - barWidth / 2
                drawRoundRect(
                    color = RadioColors.OnGold,
                    topLeft = Offset(x, center.y - barHeight / 2),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 3),
                )
            }
        } else {
            val height = this.size.height * 0.3f
            val width = height * 0.9f
            // Shifted slightly right so the triangle looks centred.
            val left = center.x - width / 2 + width * 0.12f
            drawPath(
                path = Path().apply {
                    moveTo(left, center.y - height / 2)
                    lineTo(left + width, center.y)
                    lineTo(left, center.y + height / 2)
                    close()
                },
                color = RadioColors.OnGold,
            )
        }
    }
}
