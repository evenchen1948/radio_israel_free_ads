package com.israeliradio.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.israeliradio.app.ui.theme.RadioColors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// The radio is drawn in the coordinate space of design/img.png and scaled to fit.
private const val DESIGN_WIDTH = 1144f
private const val DESIGN_HEIGHT = 818f

private val DialCenter = Offset(804f, 386f)
private const val DIAL_RADIUS = 144f
private val PowerKnobCenter = Offset(708f, 582f)
private val TuneKnobCenter = Offset(900f, 582f)
private val LedCenter = Offset(804f, 582f)
private val GrilleTopLeft = Offset(172f, 258f)
private val GrilleSize = Size(420f, 336f)

/** The needle travels this many degrees either side of twelve o'clock. */
private const val DIAL_SWEEP = 120f
private val MinTouchTarget = 48.dp

/** Dial angle of a station: stations are spread evenly across the scale. */
private fun stationAngle(index: Int, count: Int): Float =
    if (count <= 1) 0f else -DIAL_SWEEP + 2 * DIAL_SWEEP * index / (count - 1)

private fun nearestStationIndex(angle: Float, count: Int): Int =
    if (count <= 1) 0 else ((angle + DIAL_SWEEP) / (2 * DIAL_SWEEP) * (count - 1)).roundToInt().coerceIn(0, count - 1)

/** Angle of a touch point around the centre of [size]: 0 at the top, clockwise positive. */
private fun touchAngle(position: Offset, size: IntSize): Float {
    val dx = position.x - size.width / 2f
    val dy = position.y - size.height / 2f
    return Math.toDegrees(atan2(dx, -dy).toDouble()).toFloat().coerceIn(-DIAL_SWEEP, DIAL_SWEEP)
}

/**
 * The retro radio from the design, used as the main control surface:
 * the dial tunes between stations, the speaker grille and left knob toggle
 * playback, the right knob steps to the next station, and the lamp shows status.
 */
@Composable
fun RetroRadio(
    stationCount: Int,
    selectedIndex: Int?,
    isActive: Boolean,
    isLoading: Boolean,
    playPauseLabel: String,
    nextStationLabel: String,
    tunerLabel: String,
    tunerState: String,
    onTogglePlayPause: () -> Unit,
    onSelectIndex: (Int) -> Unit,
    onNextStation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragAngle by remember { mutableStateOf<Float?>(null) }
    val restingAngle = selectedIndex?.let { stationAngle(it, stationCount) } ?: -DIAL_SWEEP
    val needleAngle by animateFloatAsState(
        targetValue = dragAngle ?: restingAngle,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
        label = "needle",
    )
    val powerKnobAngle by animateFloatAsState(
        targetValue = if (isActive) 35f else -35f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "powerKnob",
    )
    val tuneKnobAngle by animateFloatAsState(
        targetValue = 50f + (selectedIndex ?: 0) * 40f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tuneKnob",
    )
    // The pulse animation only exists while connecting, so an idle screen stays idle.
    val lampLevel = when {
        isLoading -> lampPulse()
        isActive -> 1f
        else -> 0f
    }

    val currentOnSelectIndex by rememberUpdatedState(onSelectIndex)

    // The illustration has a fixed geometry, so it is laid out LTR even in the RTL app.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(modifier = modifier.aspectRatio(DESIGN_WIDTH / DESIGN_HEIGHT)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                scale(scale = size.width / DESIGN_WIDTH, pivot = Offset.Zero) {
                    drawCabinet()
                    drawGrille()
                    drawDial(stationCount, needleAngle)
                    drawKnob(PowerKnobCenter, powerKnobAngle)
                    drawKnob(TuneKnobCenter, tuneKnobAngle)
                    drawLamp(lampLevel)
                }
            }

            // Speaker grille: the large, easy target for play/pause.
            HitArea(
                center = GrilleTopLeft + Offset(GrilleSize.width / 2, GrilleSize.height / 2),
                designWidth = GrilleSize.width,
                designHeight = GrilleSize.height,
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 12))
                    .clickable(onClickLabel = playPauseLabel, role = Role.Button, onClick = onTogglePlayPause)
                    .semantics { contentDescription = playPauseLabel },
            )

            // Tuning dial: tap or drag to a station.
            HitArea(
                center = DialCenter,
                designWidth = DIAL_RADIUS * 2,
                designHeight = DIAL_RADIUS * 2,
                modifier = Modifier
                    .clip(CircleShape)
                    .pointerInput(stationCount) {
                        detectTapGestures { position ->
                            currentOnSelectIndex(nearestStationIndex(touchAngle(position, size), stationCount))
                        }
                    }
                    .pointerInput(stationCount) {
                        detectDragGestures(
                            onDragStart = { position -> dragAngle = touchAngle(position, size) },
                            onDrag = { change, _ -> dragAngle = touchAngle(change.position, size) },
                            onDragEnd = {
                                dragAngle?.let { currentOnSelectIndex(nearestStationIndex(it, stationCount)) }
                                dragAngle = null
                            },
                            onDragCancel = { dragAngle = null },
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
            )

            // Left knob mirrors the grille; hidden from TalkBack to avoid a duplicate control.
            HitArea(
                center = PowerKnobCenter,
                designWidth = 68f,
                designHeight = 68f,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onTogglePlayPause)
                    .clearAndSetSemantics { },
            )

            HitArea(
                center = TuneKnobCenter,
                designWidth = 68f,
                designHeight = 68f,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClickLabel = nextStationLabel, role = Role.Button, onClick = onNextStation)
                    .semantics { contentDescription = nextStationLabel },
            )
        }
    }
}

@Composable
private fun lampPulse(): Float {
    val pulse by rememberInfiniteTransition(label = "lamp").animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 600), RepeatMode.Reverse),
        label = "lampPulse",
    )
    return pulse
}

/** Transparent touch target centred on a point of the design, never smaller than 48dp. */
@Composable
private fun BoxWithConstraintsScope.HitArea(
    center: Offset,
    designWidth: Float,
    designHeight: Float,
    modifier: Modifier,
) {
    val unit: Dp = maxWidth / DESIGN_WIDTH
    val width = max(unit * designWidth, MinTouchTarget)
    val height = max(unit * designHeight, MinTouchTarget)
    Box(
        modifier = Modifier
            .offset(x = unit * center.x - width / 2, y = unit * center.y - height / 2)
            .size(width, height)
            .then(modifier),
    )
}

private fun DrawScope.drawCabinet() {
    // Carry handle and its brass mounts.
    drawRoundRect(
        color = RadioColors.BodyEdge,
        topLeft = Offset(393f, 46f),
        size = Size(359f, 200f),
        cornerRadius = CornerRadius(80f),
        style = Stroke(width = 28f),
    )
    for (x in listOf(368f, 724f)) {
        drawRoundRect(RadioColors.Gold, Offset(x, 122f), Size(52f, 30f), CornerRadius(8f))
    }

    // Feet, then the body with its darker edge.
    for (x in listOf(152f, 872f)) {
        drawRoundRect(RadioColors.Feet, Offset(x, 700f), Size(120f, 50f), CornerRadius(10f))
    }
    drawRoundRect(RadioColors.BodyEdge, Offset(52f, 142f), Size(1040f, 580f), CornerRadius(120f))
    drawRoundRect(RadioColors.Body, Offset(68f, 150f), Size(1008f, 548f), CornerRadius(110f))
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = Offset(152f, 178f),
        end = Offset(992f, 178f),
        strokeWidth = 10f,
        cap = StrokeCap.Round,
    )

    // Cream faceplate with a brass rim.
    drawRoundRect(RadioColors.Gold, Offset(122f, 212f), Size(900f, 428f), CornerRadius(60f))
    drawRoundRect(RadioColors.Cream, Offset(128f, 218f), Size(888f, 416f), CornerRadius(54f))
}

private fun DrawScope.drawGrille() {
    drawRoundRect(RadioColors.Brown, GrilleTopLeft, GrilleSize, CornerRadius(40f))
    for (row in 0 until 9) {
        val y = 294f + row * 32f
        drawLine(RadioColors.Slat, Offset(199f, y), Offset(565f, y), strokeWidth = 10f, cap = StrokeCap.Round)
    }
    drawRect(RadioColors.Gold, Offset(368f, 258f), Size(28f, 336f))
}

private fun DrawScope.drawDial(stationCount: Int, needleAngle: Float) {
    drawCircle(RadioColors.Gold, DIAL_RADIUS, DialCenter)
    drawCircle(RadioColors.DialFace, DIAL_RADIUS - 12f, DialCenter)

    // One major tick per station, with a minor tick between neighbours.
    val majors = stationCount.coerceAtLeast(2)
    for (i in 0 until majors) {
        val angle = stationAngle(i, majors)
        drawTick(angle, innerRadius = 92f, strokeWidth = 5f, color = RadioColors.Brown)
        if (i < majors - 1) {
            val next = stationAngle(i + 1, majors)
            drawTick((angle + next) / 2, innerRadius = 104f, strokeWidth = 3f, color = RadioColors.MinorTick)
        }
    }

    rotate(degrees = needleAngle, pivot = DialCenter) {
        drawLine(
            color = RadioColors.Red,
            start = DialCenter,
            end = DialCenter - Offset(0f, 100f),
            strokeWidth = 8f,
            cap = StrokeCap.Round,
        )
    }

    drawCircle(RadioColors.BodyEdge, 44f, DialCenter)
    drawCircle(RadioColors.Body, 28f, DialCenter)
    drawCircle(RadioColors.Gold, 8f, DialCenter)
}

private fun DrawScope.drawTick(angle: Float, innerRadius: Float, strokeWidth: Float, color: Color) {
    val radians = Math.toRadians(angle.toDouble())
    val direction = Offset(sin(radians).toFloat(), -cos(radians).toFloat())
    drawLine(
        color = color,
        start = DialCenter + direction * innerRadius,
        end = DialCenter + direction * 118f,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawKnob(center: Offset, angle: Float) {
    drawCircle(RadioColors.Gold, 34f, center)
    drawCircle(RadioColors.Brown, 26f, center)
    rotate(degrees = angle, pivot = center) {
        drawLine(
            color = RadioColors.Cream,
            start = center,
            end = center - Offset(0f, 20f),
            strokeWidth = 5f,
            cap = StrokeCap.Round,
        )
    }
}

/** Status lamp: [level] 0 is off, 1 is fully lit. */
private fun DrawScope.drawLamp(level: Float) {
    if (level > 0f) {
        drawCircle(RadioColors.Red.copy(alpha = 0.25f * level), 22f, LedCenter)
    }
    drawCircle(RadioColors.RedOff, 10f, LedCenter)
    drawCircle(RadioColors.Red.copy(alpha = level), 10f, LedCenter)
}
