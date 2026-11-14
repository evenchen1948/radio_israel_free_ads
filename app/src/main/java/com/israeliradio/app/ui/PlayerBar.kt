package com.israeliradio.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.israeliradio.app.data.Station
import com.israeliradio.app.ui.theme.RadioColors

/** Height the bar occupies, used to reserve space under the list so nothing shifts when it appears. */
val PlayerBarHeight = 72.dp

/** Floating bar for the current station: small tile, name, status and the play/pause button. */
@Composable
fun PlayerBar(
    station: Station,
    statusText: String,
    isError: Boolean,
    isActive: Boolean,
    playPauseLabel: String,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(RadioColors.BlueTint, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = station.shortLabel(),
                    color = RadioColors.Blue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
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
            Canvas(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(RadioColors.Blue)
                    .clickable(role = Role.Button, onClickLabel = playPauseLabel, onClick = onTogglePlayPause)
                    .semantics { contentDescription = playPauseLabel },
            ) {
                if (isActive) {
                    val barWidth = size.width * 0.085f
                    val barHeight = size.height * 0.34f
                    val gap = size.width * 0.075f
                    for (side in listOf(-1f, 1f)) {
                        val x = center.x + side * (gap + barWidth) / 2 - barWidth / 2
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(x, center.y - barHeight / 2),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 3),
                        )
                    }
                } else {
                    val height = size.height * 0.36f
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
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * A one-or-two character stand-in for the station logo: the number in the
 * name when there is one, otherwise the start of its last word.
 */
private fun Station.shortLabel(): String =
    Regex("""\d+(\.\d+)?""").find(name)?.value
        ?: name.trim().substringAfterLast(' ').take(2)
