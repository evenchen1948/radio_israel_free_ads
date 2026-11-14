package com.israeliradio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Palette sampled from the retro radio illustration in design/img.png. */
object RadioColors {
    val Background = Color(0xFF151515)
    val Body = Color(0xFF7FB5A6)
    val BodyEdge = Color(0xFF3F7466)
    val Feet = Color(0xFF2F5247)
    val Gold = Color(0xFFC9A24B)
    val Cream = Color(0xFFF3E9D5)
    val DialFace = Color(0xFFFBF5E9)
    val Brown = Color(0xFF6B4A33)
    val Slat = Color(0xFFD9B88A)
    val MinorTick = Color(0xFFA88B63)
    val Red = Color(0xFFC4362B)
    val RedOff = Color(0xFF6E2620)
    val MutedText = Color(0xFFA9A397)
}

private val RadioColorScheme = darkColorScheme(
    primary = RadioColors.Body,
    onPrimary = RadioColors.Background,
    secondary = RadioColors.Gold,
    onSecondary = RadioColors.Background,
    background = RadioColors.Background,
    onBackground = RadioColors.Cream,
    surface = RadioColors.Background,
    onSurface = RadioColors.Cream,
    onSurfaceVariant = RadioColors.MutedText,
    error = RadioColors.Red,
)

/** Fixed app theme; dynamic color is deliberately not used. */
@Composable
fun RadioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RadioColorScheme, content = content)
}
