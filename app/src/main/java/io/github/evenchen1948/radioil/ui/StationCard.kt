package io.github.evenchen1948.radioil.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.evenchen1948.radioil.data.Station
import io.github.evenchen1948.radioil.ui.theme.RadioColors
import io.github.evenchen1948.radioil.ui.theme.SerifFamily

private val TileShape = RoundedCornerShape(16.dp)
private val TileHeight = 116.dp

/**
 * A station as a large rounded tile with its name and a subtitle underneath.
 * The tile is text only (see [tileLabel]); the playing station is outlined in gold.
 */
@Composable
fun StationCard(
    station: Station,
    subtitle: String,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardPress",
    )

    Column(
        modifier = modifier
            .scale(pressScale)
            .clip(TileShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) { selected = isCurrent },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TileHeight)
                .background(RadioColors.Surface, TileShape)
                .border(1.dp, if (isCurrent) RadioColors.Gold else RadioColors.Outline, TileShape),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                StationTileLabel(
                    station = station,
                    mainColor = if (isCurrent) RadioColors.Gold else RadioColors.Text,
                    maxMainSize = 44.sp,
                    captionSize = 11.sp,
                    captionGap = 2.dp,
                )
                if (isCurrent) {
                    Equalizer(
                        animate = isPlaying,
                        modifier = Modifier.align(Alignment.BottomStart),
                    )
                }
            }
            // Its own control: a tap here stars the station without playing it.
            FavoriteToggle(
                isFavorite = isFavorite,
                onToggle = onToggleFavorite,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }

        Text(
            text = station.name,
            color = RadioColors.Text,
            fontSize = 17.sp,
            fontFamily = SerifFamily,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            text = subtitle,
            color = RadioColors.SecondaryText,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
        )
    }
}

private val EqualizerRestingHeights = listOf(0.55f, 1f, 0.4f)

/** Three small bars that bounce while the station is playing and rest otherwise. */
@Composable
private fun Equalizer(animate: Boolean, modifier: Modifier = Modifier, color: Color = RadioColors.Gold) {
    val heights = if (animate) animatedBarHeights() else EqualizerRestingHeights

    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        heights.forEach { fraction ->
            Spacer(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight(fraction)
                    .background(color, RoundedCornerShape(2.dp)),
            )
        }
    }
}

/** The animation only exists while playing, so a paused screen stays idle. */
@Composable
private fun animatedBarHeights(): List<Float> {
    val transition = rememberInfiniteTransition(label = "equalizer")
    return listOf(420, 560, 360).mapIndexed { index, duration ->
        val height by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = duration), RepeatMode.Reverse),
            label = "bar$index",
        )
        height
    }
}
