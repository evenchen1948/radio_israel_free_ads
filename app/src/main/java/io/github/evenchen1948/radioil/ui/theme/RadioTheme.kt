package io.github.evenchen1948.radioil.ui.theme

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import io.github.evenchen1948.radioil.R
import io.github.evenchen1948.radioil.data.AppTheme

/** The colors of one theme. Only colors differ between themes. */
class RadioPalette(
    /** The screen background is a vertical gradient between these two; the dark theme's is flat. */
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val surface: Color,
    val outline: Color,
    val gold: Color,
    val onGold: Color,
    val goldText: Color,
    val track: Color,
    val text: Color,
    val secondaryText: Color,
    val error: Color,
    /** Drawn over the favorites artwork, with its transparency, so text on it keeps its contrast. */
    val artworkScrim: Color,
) {
    val backgroundBrush: Brush get() = Brush.verticalGradient(listOf(backgroundTop, backgroundBottom))
}

/** Sampled from the player design in design/2.png. */
private val DarkPalette = RadioPalette(
    backgroundTop = Color(0xFF0A0A0C),
    backgroundBottom = Color(0xFF0A0A0C),
    surface = Color(0xFF16161A),
    outline = Color(0xFF2A2A2E),
    gold = Color(0xFFC8A96A),
    onGold = Color(0xFF0A0A0C),
    goldText = Color(0xFFC8A96A),
    track = Color(0xFF3A3A3C),
    text = Color(0xFFF2EEE6),
    secondaryText = Color(0xFF8E8E93),
    error = Color(0xFFEF8A80),
    artworkScrim = Color(0x99000000),
)

/**
 * Pearl white: a warm white canvas with pure white surfaces. Every text color
 * keeps at least 4.5:1 against the darkest point of the background gradient
 * (text 15.2:1, secondary text 6.2:1, gold text 5.2:1, error 6.3:1), and the
 * marks on gold keep 8.8:1.
 *
 * Over the favorites artwork the 70% pearl scrim leaves the darkest parts of an
 * illustration at about #AEADA9, where only the primary text color still passes
 * (7.9:1; secondary 3.2:1, gold 2.7:1), so text on the artwork is drawn in the
 * primary color.
 */
private val PearlPalette = RadioPalette(
    backgroundTop = Color(0xFFF8F6F1),
    backgroundBottom = Color(0xFFF1EEE7),
    surface = Color(0xFFFFFFFF),
    outline = Color(0xFFE4DFD4),
    gold = Color(0xFFC8A96A),
    onGold = Color(0xFF0A0A0C),
    goldText = Color(0xFF7A5F22),
    track = Color(0xFFE4DFD4),
    text = Color(0xFF1A1814),
    secondaryText = Color(0xFF5C574E),
    error = Color(0xFFA8201A),
    artworkScrim = Color(0xB3F8F6F1),
)

/** The colors of [theme], for drawing a preview of a theme that is not the active one. */
fun paletteOf(theme: AppTheme): RadioPalette = when (theme) {
    AppTheme.DARK -> DarkPalette
    AppTheme.PEARL -> PearlPalette
}

/**
 * The colors of the active theme. They are backed by snapshot state, so
 * anything that reads them, while composing or while drawing, is updated when
 * the theme changes; during the change every color is a blend of the two themes.
 */
object RadioColors {
    /** 0 in the dark theme, 1 in the pearl theme, in between while one fades into the other. */
    private var pearlFraction by mutableFloatStateOf(0f)

    private inline fun blend(color: (RadioPalette) -> Color): Color =
        lerp(color(DarkPalette), color(PearlPalette), pearlFraction)

    /** The top of the background gradient; also the fill of small insets such as the bar's disc. */
    val Background: Color get() = blend { it.backgroundTop }

    /** The screen background. */
    val BackgroundBrush: Brush
        get() = Brush.verticalGradient(listOf(blend { it.backgroundTop }, blend { it.backgroundBottom }))

    /** Tiles, discs, the player bar and sheets. */
    val Surface: Color get() = blend { it.surface }

    /** Hairline around surfaces and round buttons. */
    val Outline: Color get() = blend { it.outline }

    /** The accent as a filled shape: the play button, the selected chip, the disc ring, the slider. */
    val Gold: Color get() = blend { it.gold }

    /** Marks and text drawn on [Gold]. */
    val OnGold: Color get() = blend { it.onGold }

    /** The accent as text or a thin mark on the background: links, the live indicator, the star. */
    val GoldText: Color get() = blend { it.goldText }

    /** Unfilled part of the volume slider. */
    val Track: Color get() = blend { it.track }

    val Text: Color get() = blend { it.text }
    val SecondaryText: Color get() = blend { it.secondaryText }
    val Error: Color get() = blend { it.error }

    /** Hairline between rows: the primary text color at 10%. */
    val Divider: Color get() = Text.copy(alpha = 0.10f)

    /**
     * Text drawn directly on the favorites artwork. Under the scrim only the
     * primary text color keeps 4.5:1 there, so grey and gold text use it too.
     */
    val OnArtwork: Color get() = Text

    /** Covers the favorites artwork: black in the dark theme, pearl in the pearl theme. */
    val ArtworkScrim: Color get() = blend { it.artworkScrim }

    /** Surfaces lift slightly off the pearl background; on the dark one they lie flat. */
    internal val SurfaceElevation: Dp get() = 3.dp * pearlFraction

    internal val isLight: Boolean get() = pearlFraction > 0.5f

    internal fun snapTo(theme: AppTheme) {
        pearlFraction = fractionOf(theme)
    }

    internal suspend fun fadeTo(theme: AppTheme) {
        animate(pearlFraction, fractionOf(theme), animationSpec = tween(250)) { value, _ ->
            pearlFraction = value
        }
    }

    private fun fractionOf(theme: AppTheme) = if (theme == AppTheme.PEARL) 1f else 0f
}

/** The very soft warm shadow under a surface in the pearl theme; nothing in the dark theme. */
fun Modifier.surfaceShadow(shape: Shape): Modifier = shadow(
    elevation = RadioColors.SurfaceElevation,
    shape = shape,
    clip = false,
    ambientColor = Color(0x4D5C4B2A),
    spotColor = Color(0x4D5C4B2A),
)

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

/**
 * The app theme in the colors of [theme]; a change of theme fades in over
 * 250ms. Dynamic color is deliberately not used.
 */
@Composable
fun RadioTheme(theme: AppTheme, content: @Composable () -> Unit) {
    // The first theme is applied before anything is drawn; later ones fade in.
    remember { RadioColors.snapTo(theme) }
    LaunchedEffect(theme) { RadioColors.fadeTo(theme) }

    val colorScheme = if (RadioColors.isLight) {
        lightColorScheme(
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
    } else {
        darkColorScheme(
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
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

/**
 * Sets whether the status and navigation bar icons are dark, for whatever is
 * behind them right now. The bars themselves are always transparent.
 */
@Composable
fun SystemBarIcons(dark: Boolean) {
    val window = LocalActivity.current?.window ?: return
    val view = LocalView.current
    SideEffect {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = dark
            isAppearanceLightNavigationBars = dark
        }
    }
}
