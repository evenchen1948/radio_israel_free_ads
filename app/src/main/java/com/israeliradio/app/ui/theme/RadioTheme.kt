package com.israeliradio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Palette sampled from the tuner design in design/look.jpeg. */
object RadioColors {
    /** Display panel behind the station name. */
    val Slate = Color(0xFF3B4048)

    /** Tuner scale strip, slightly darker than the display. */
    val SlateDark = Color(0xFF2F333A)
    val ScaleDivider = Color(0xFF24272C)
    val ScaleLabel = Color(0xFFA9ADB3)
    val ScaleTick = Color(0xFF6E737B)
    val Indicator = Color(0xFFE0524D)

    val Lime = Color(0xFFA8CF45)
    val DisplayText = Color(0xFFFFFFFF)
    val DisplayMuted = Color(0xFFA9ADB3)

    /** Light area behind the transport controls and the station list. */
    val Surface = Color(0xFFF2F2F2)
    val ButtonRing = Color(0xFFE3E3E3)
    val ButtonFaceTop = Color(0xFFFFFFFF)
    val ButtonFaceBottom = Color(0xFFECECEC)
    val Icon = Color(0xFF3F444B)

    val PresetBorder = Color(0xFFD9D9D9)
    val MutedText = Color(0xFF6E737B)
}

private val RadioColorScheme = lightColorScheme(
    primary = RadioColors.Slate,
    onPrimary = Color.White,
    secondary = RadioColors.Lime,
    onSecondary = RadioColors.Slate,
    background = RadioColors.Surface,
    onBackground = RadioColors.Icon,
    surface = Color.White,
    onSurface = RadioColors.Icon,
    onSurfaceVariant = RadioColors.MutedText,
    error = RadioColors.Indicator,
)

/** Fixed app theme; dynamic color is deliberately not used. */
@Composable
fun RadioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RadioColorScheme, content = content)
}
