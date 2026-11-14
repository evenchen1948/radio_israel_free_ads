package com.israeliradio.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.israeliradio.app.ui.theme.RadioColors
import kotlin.math.roundToInt

/**
 * Dark display at the top of the player: a status label, the station name in
 * very large light type, and a reserved bottom row that holds the retry action.
 * Every row has a fixed height so nothing shifts when the text changes.
 */
@Composable
fun PlayerDisplay(
    stationName: String,
    statusLabel: String,
    statusColor: Color,
    retryLabel: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RadioColors.Slate)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = statusLabel,
            color = statusColor,
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Shrinks long station names to fit one line instead of wrapping.
            BasicText(
                text = stationName,
                style = TextStyle(
                    color = RadioColors.DisplayText,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 24.sp, maxFontSize = 84.sp, stepSize = 2.sp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (retryLabel != null) {
                TextButton(onClick = onRetry) {
                    Text(text = retryLabel, color = RadioColors.Lime, fontSize = 16.sp)
                }
            }
        }
    }
}

private val ScaleHeight = 62.dp
private val ScaleSidePadding = 24.dp

/** How often a station tick is tall and numbered. */
private const val LABEL_EVERY = 3

/**
 * Tuner scale: one tick per station and a red line on the selected one.
 * Tap or drag to tune. Drawn left-to-right like a physical tuner, in any layout direction.
 */
@Composable
fun TunerScale(
    stationCount: Int,
    selectedIndex: Int?,
    tunerLabel: String,
    tunerState: String,
    nextStationLabel: String,
    onSelectIndex: (Int) -> Unit,
    onNextStation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastIndex = (stationCount - 1).coerceAtLeast(1)
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val restingFraction = (selectedIndex ?: 0).toFloat() / lastIndex
    val indicatorFraction by animateFloatAsState(
        targetValue = dragFraction ?: restingFraction,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "indicator",
    )
    val currentOnSelectIndex by rememberUpdatedState(onSelectIndex)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = RadioColors.ScaleLabel, fontSize = 17.sp)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(ScaleHeight)
                .background(RadioColors.SlateDark)
                .pointerInput(stationCount) {
                    val pad = ScaleSidePadding.toPx()
                    fun fractionAt(x: Float) = ((x - pad) / (size.width - 2 * pad)).coerceIn(0f, 1f)
                    detectTapGestures { position ->
                        currentOnSelectIndex((fractionAt(position.x) * lastIndex).roundToInt())
                    }
                }
                .pointerInput(stationCount) {
                    val pad = ScaleSidePadding.toPx()
                    fun fractionAt(x: Float) = ((x - pad) / (size.width - 2 * pad)).coerceIn(0f, 1f)
                    detectHorizontalDragGestures(
                        onDragStart = { position -> dragFraction = fractionAt(position.x) },
                        onHorizontalDrag = { change, _ -> dragFraction = fractionAt(change.position.x) },
                        onDragEnd = {
                            dragFraction?.let { currentOnSelectIndex((it * lastIndex).roundToInt()) }
                            dragFraction = null
                        },
                        onDragCancel = { dragFraction = null },
                    )
                }
                .semantics {
                    contentDescription = tunerLabel
                    stateDescription = tunerState
                    role = Role.Button
                    onClick(label = nextStationLabel) {
                        onNextStation()
                        true
                    }
                },
        ) {
            val pad = ScaleSidePadding.toPx()
            val usable = size.width - 2 * pad
            fun xOf(fraction: Float) = pad + fraction * usable

            drawRect(RadioColors.ScaleDivider, size = Size(size.width, 1.dp.toPx()))

            for (i in 0 until stationCount.coerceAtLeast(2)) {
                val x = xOf(i.toFloat() / lastIndex)
                val major = i % LABEL_EVERY == 0
                val tickHeight = (if (major) 16.dp else 9.dp).toPx()
                drawLine(
                    color = RadioColors.ScaleTick,
                    start = Offset(x, size.height - tickHeight),
                    end = Offset(x, size.height),
                    strokeWidth = 1.5.dp.toPx(),
                )
                if (major) {
                    val label = textMeasurer.measure((i + 1).toString(), labelStyle)
                    drawText(label, topLeft = Offset(x - label.size.width / 2f, 12.dp.toPx()))
                }
            }

            // No line until a station is tuned.
            if (selectedIndex == null && dragFraction == null) return@Canvas
            val indicatorX = xOf(indicatorFraction)
            drawLine(
                color = RadioColors.Indicator,
                start = Offset(indicatorX, 0f),
                end = Offset(indicatorX, size.height),
                strokeWidth = 2.dp.toPx(),
            )
        }
    }
}

/**
 * Previous, play/stop and next as soft raised round buttons. Media controls keep
 * their left-to-right order in RTL, as on a physical player.
 */
@Composable
fun TransportControls(
    isActive: Boolean,
    playPauseLabel: String,
    previousLabel: String,
    nextLabel: String,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftRoundButton(outerSize = 80.dp, faceSize = 58.dp, label = previousLabel, onClick = onPrevious) {
                drawSkipIcon(forward = false)
            }
            SoftRoundButton(outerSize = 140.dp, faceSize = 112.dp, label = playPauseLabel, onClick = onTogglePlayPause) {
                if (isActive) drawStopIcon() else drawPlayIcon()
            }
            SoftRoundButton(outerSize = 80.dp, faceSize = 58.dp, label = nextLabel, onClick = onNext) {
                drawSkipIcon(forward = true)
            }
        }
    }
}

/** A pale ring holding a raised white disc, with the icon drawn in the disc's centre. */
@Composable
private fun SoftRoundButton(
    outerSize: Dp,
    faceSize: Dp,
    label: String,
    onClick: () -> Unit,
    drawIcon: DrawScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press",
    )

    Box(
        modifier = Modifier
            .size(outerSize)
            .background(RadioColors.ButtonRing, CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(faceSize)
                .scale(pressScale)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .background(
                    Brush.verticalGradient(listOf(RadioColors.ButtonFaceTop, RadioColors.ButtonFaceBottom)),
                    CircleShape,
                ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize(), onDraw = drawIcon)
        }
    }
}

private fun DrawScope.drawStopIcon() {
    val side = size.minDimension * 0.34f
    drawRoundRect(
        color = RadioColors.Icon,
        topLeft = Offset(center.x - side / 2, center.y - side / 2),
        size = Size(side, side),
        cornerRadius = CornerRadius(side * 0.06f),
    )
}

private fun DrawScope.drawPlayIcon() {
    val height = size.minDimension * 0.38f
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
        color = RadioColors.Icon,
    )
}

/** Two triangles pointing right ([forward]) or left. */
private fun DrawScope.drawSkipIcon(forward: Boolean) {
    val height = size.minDimension * 0.30f
    val width = height * 0.85f
    val direction = if (forward) 1f else -1f
    val start = center.x - direction * width
    val path = Path()
    for (i in 0..1) {
        val base = start + direction * width * i
        path.moveTo(base, center.y - height / 2)
        path.lineTo(base + direction * width, center.y)
        path.lineTo(base, center.y + height / 2)
        path.close()
    }
    drawPath(path, RadioColors.Icon)
}
