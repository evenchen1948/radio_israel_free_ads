package com.israeliradio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Palette sampled from the home screen design in design/1.png. */
object RadioColors {
    val Background = Color(0xFFFFFFFF)

    /** The single accent: selected chip, links, the playing station, the play button. */
    val Blue = Color(0xFF0077B5)

    /** Tile behind the playing station. */
    val BlueTint = Color(0xFFE1F1FA)

    /** Text and marks drawn on [BlueTint]; plain [Blue] is below 4.5:1 contrast there. */
    val BlueOnTint = Color(0xFF005F91)

    /** Neutral tiles and unselected chips. */
    val Tile = Color(0xFFF3F5F7)

    val Text = Color(0xFF1F2024)
    val SecondaryText = Color(0xFF6F7278)
    val Error = Color(0xFFC62828)
}

private val RadioColorScheme = lightColorScheme(
    primary = RadioColors.Blue,
    onPrimary = Color.White,
    background = RadioColors.Background,
    onBackground = RadioColors.Text,
    surface = RadioColors.Background,
    onSurface = RadioColors.Text,
    surfaceVariant = RadioColors.Tile,
    onSurfaceVariant = RadioColors.SecondaryText,
    error = RadioColors.Error,
)

/** Fixed app theme; dynamic color is deliberately not used. */
@Composable
fun RadioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RadioColorScheme, content = content)
}
