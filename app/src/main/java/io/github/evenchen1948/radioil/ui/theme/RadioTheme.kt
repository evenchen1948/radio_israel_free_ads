package io.github.evenchen1948.radioil.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import io.github.evenchen1948.radioil.R

/** Palette sampled from the player design in design/2.png. */
object RadioColors {
    val Background = Color(0xFF0A0A0C)

    /** Tiles, discs and the player bar: one step lighter than the background. */
    val Surface = Color(0xFF16161A)

    /** Hairline around surfaces and round buttons. */
    val Outline = Color(0xFF2A2A2E)

    /** The single accent: play button, active station, live indicator, selected chip, links. */
    val Gold = Color(0xFFC8A96A)

    /** Marks and text drawn on [Gold]. */
    val OnGold = Color(0xFF0A0A0C)

    /** Unfilled part of the volume slider. */
    val Track = Color(0xFF3A3A3C)

    val Text = Color(0xFFF2EEE6)
    val SecondaryText = Color(0xFF8E8E93)
    val Error = Color(0xFFEF8A80)
}

/** Frank Ruhl Libre (variable font): greeting, section titles, station names and tile labels. */
val SerifFamily = FontFamily(
    listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.Bold).map { weight ->
        Font(
            resId = R.font.frank_ruhl_libre,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
        )
    },
)

private val RadioColorScheme = darkColorScheme(
    primary = RadioColors.Gold,
    onPrimary = RadioColors.OnGold,
    background = RadioColors.Background,
    onBackground = RadioColors.Text,
    surface = RadioColors.Background,
    onSurface = RadioColors.Text,
    surfaceVariant = RadioColors.Surface,
    onSurfaceVariant = RadioColors.SecondaryText,
    outline = RadioColors.Outline,
    error = RadioColors.Error,
)

/** Fixed dark app theme; dynamic color is deliberately not used. */
@Composable
fun RadioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RadioColorScheme, content = content)
}
