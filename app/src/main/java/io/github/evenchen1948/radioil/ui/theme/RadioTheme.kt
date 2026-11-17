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
)

/**
 * Brushed silver. Every text color keeps at least 4.5:1 against the darkest
 * point of the background gradient: text 12.4:1, secondary text 5.8:1, gold
 * text 4.6:1, error 5.1:1.
 */
private val SilverPalette = RadioPalette(
    backgroundTop = Color(0xFFF1F2F4),
    backgroundBottom = Color(0xFFD5D8DC),
    surface = Color(0xFFF8F9FA),
    outline = Color(0xFFB4B9BF),
    gold = Color(0xFFC8A96A),
    onGold = Color(0xFF0A0A0C),
    goldText = Color(0xFF73591F),
    track = Color(0xFFB4B9BF),
    text = Color(0xFF16181D),
    secondaryText = Color(0xFF4A4F57),
    error = Color(0xFFA8201A),
)

/** The colors of [theme], for drawing a preview of a theme that is not the active one. */
fun paletteOf(theme: AppTheme): RadioPalette = when (theme) {
    AppTheme.DARK -> DarkPalette
    AppTheme.SILVER -> SilverPalette
}

/**
 * The colors of the active theme. They are backed by snapshot state, so
 * anything that reads them, while composing or while drawing, is updated when
 * the theme changes; during the change every color is a blend of the two themes.
 */
object RadioColors {
    /** 0 in the dark theme, 1 in the silver theme, in between while one fades into the other. */
    private var silverFraction by mutableFloatStateOf(0f)

    private inline fun blend(color: (RadioPalette) -> Color): Color =
        lerp(color(DarkPalette), color(SilverPalette), silverFraction)

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

    /** Text drawn on the favorites artwork, which is dark in both themes. */
    val OnArtwork = Color(0xFFF2EEE6)

    /** The plate behind text on the favorites artwork, before its transparency. */
    val ArtworkPlate = Color(0xFF0A0A0C)

    /** Surfaces lift slightly off the silver background; on the dark one they lie flat. */
    internal val SurfaceElevation: Dp get() = 3.dp * silverFraction

    internal val isSilver: Boolean get() = silverFraction > 0.5f

    internal fun snapTo(theme: AppTheme) {
        silverFraction = fractionOf(theme)
    }

    internal suspend fun fadeTo(theme: AppTheme) {
        animate(silverFraction, fractionOf(theme), animationSpec = tween(250)) { value, _ ->
            silverFraction = value
        }
    }

    private fun fractionOf(theme: AppTheme) = if (theme == AppTheme.SILVER) 1f else 0f
}

/** The very soft shadow under a surface in the silver theme; nothing in the dark theme. */
fun Modifier.surfaceShadow(shape: Shape): Modifier = shadow(
    elevation = RadioColors.SurfaceElevation,
    shape = shape,
    clip = false,
    ambientColor = Color(0x5916181D),
    spotColor = Color(0x5916181D),
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

    val colorScheme = if (RadioColors.isSilver) {
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
