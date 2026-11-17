package io.github.evenchen1948.radioil.ui

import android.content.res.Resources
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import io.github.evenchen1948.radioil.R
import io.github.evenchen1948.radioil.data.AppTheme
import io.github.evenchen1948.radioil.data.FavoritesBackground
import io.github.evenchen1948.radioil.ui.theme.RadioColors
import io.github.evenchen1948.radioil.ui.theme.RadioPalette
import io.github.evenchen1948.radioil.ui.theme.SerifFamily
import io.github.evenchen1948.radioil.ui.theme.paletteOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val CardWidth = 120.dp

/**
 * Width over height of a card. Much shorter than the illustrations, so the
 * sheet leaves the upper part of the tab, and the change behind it, in view.
 */
private const val CARD_ASPECT_RATIO = 10f / 11f

/** Crops to the band of an illustration where the radio stands, from its antenna to a little floor under it. */
private val CardImageAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = 0.45f)
private val CardShape = RoundedCornerShape(14.dp)
private val ThemeTileHeight = 52.dp

/** The round button on the favorites tab that opens the picker. */
@Composable
fun ChangeBackgroundButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedRoundButton(
        label = stringResource(R.string.action_change_background),
        onClick = onClick,
        modifier = modifier,
        // Filled like the chips, so it stays visible over the artwork.
        fill = RadioColors.Surface,
    ) { drawPictureIcon() }
}

/** A framed landscape: two hills and a sun. */
private fun DrawScope.drawPictureIcon() {
    val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val width = 20.dp.toPx()
    val height = 16.dp.toPx()
    val left = center.x - width / 2
    val top = center.y - height / 2
    drawRoundRect(
        color = RadioColors.Text,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(3.dp.toPx()),
        style = stroke,
    )
    val hills = Path().apply {
        moveTo(left + width * 0.12f, top + height * 0.82f)
        lineTo(left + width * 0.38f, top + height * 0.45f)
        lineTo(left + width * 0.56f, top + height * 0.68f)
        lineTo(left + width * 0.68f, top + height * 0.55f)
        lineTo(left + width * 0.88f, top + height * 0.82f)
    }
    drawPath(hills, RadioColors.Text, style = stroke)
    drawCircle(RadioColors.Text, radius = 1.5.dp.toPx(), center = Offset(left + width * 0.72f, top + height * 0.3f))
}

/**
 * Bottom sheet for how the app looks: two tiles for the color theme, then one
 * preview card per favorites background. A tap applies the choice at once and
 * the sheet stays open, so several can be tried against the real screen; for
 * the same reason the sheet does not dim what is behind it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundPickerSheet(
    theme: AppTheme,
    selected: FavoritesBackground?,
    /** Whether the screen behind the sheet needs dark status bar icons. */
    darkStatusBarIcons: Boolean,
    onThemeChange: (AppTheme) -> Unit,
    onSelect: (FavoritesBackground) -> Unit,
    onDismiss: () -> Unit,
) {
    val listState = rememberLazyListState(
        // Open with the current choice in view, even when it is a late decade.
        initialFirstVisibleItemIndex = selected?.let(FavoritesBackground.entries::indexOf) ?: 0,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RadioColors.Surface,
        contentColor = RadioColors.Text,
        // Still catches the tap outside that closes the sheet.
        scrimColor = Color.Transparent,
        // The sheet is its own window: its bar icons follow the screen behind the
        // status bar and the sheet's own surface behind the navigation bar.
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = darkStatusBarIcons,
            isAppearanceLightNavigationBars = theme == AppTheme.PEARL,
        ),
    ) {
        SheetBarIcons(darkStatusBar = darkStatusBarIcons, darkNavigationBar = theme == AppTheme.PEARL)
        SheetHeading(stringResource(R.string.theme_setting))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppTheme.entries.forEach { option ->
                ThemeTile(
                    theme = option,
                    selected = option == theme,
                    onClick = { onThemeChange(option) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SheetHeading(stringResource(R.string.background_picker_title))
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Start),
        ) {
            items(FavoritesBackground.entries, key = { it.id }) { background ->
                BackgroundCard(
                    background = background,
                    selected = background == selected,
                    onClick = { onSelect(background) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Keeps the bar icons of the sheet's own window right while it is open. The
 * sheet's properties only set them when it opens, so without this a theme
 * chosen in the sheet would leave the old icons until it is reopened.
 */
@Composable
private fun SheetBarIcons(darkStatusBar: Boolean, darkNavigationBar: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = generateSequence(view.parent) { it.parent }
            .filterIsInstance<DialogWindowProvider>()
            .firstOrNull()
            ?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = darkStatusBar
            isAppearanceLightNavigationBars = darkNavigationBar
        }
    }
}

@Composable
private fun SheetHeading(text: String) {
    Text(
        text = text,
        color = RadioColors.Text,
        fontSize = 18.sp,
        fontFamily = SerifFamily,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .semantics { heading() },
    )
}

/** A miniature of a theme, in that theme's own colors whichever theme is active. */
@Composable
private fun ThemeTile(theme: AppTheme, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(theme.labelRes)
    val name = stringResource(R.string.theme_description, label)
    val palette = paletteOf(theme)
    ChoiceColumn(
        label = label,
        description = if (selected) stringResource(R.string.choice_selected, name) else name,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ThemeTileHeight)
                .clip(CardShape)
                .background(palette.backgroundBrush),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(width = 80.dp, height = 32.dp)) { drawThemeSample(palette) }
            ChoiceFrame(selected)
        }
    }
}

/** A small player bar in the given colors: a surface with two lines of text and a gold play button. */
private fun DrawScope.drawThemeSample(palette: RadioPalette) {
    val corner = CornerRadius(size.height / 2)
    drawRoundRect(palette.surface, cornerRadius = corner)
    drawRoundRect(palette.outline, cornerRadius = corner, style = Stroke(width = 1.dp.toPx()))
    val buttonRadius = size.height * 0.3f
    // Drawn left to right: this is a picture of the bar, not a mirrored layout.
    drawCircle(palette.gold, radius = buttonRadius, center = Offset(size.height / 2, center.y))
    val textLeft = size.height
    val line = 3.dp.toPx()
    drawRoundRect(
        color = palette.text,
        topLeft = Offset(textLeft, center.y - line * 1.6f),
        size = Size(size.width * 0.42f, line),
        cornerRadius = CornerRadius(line / 2),
    )
    drawRoundRect(
        color = palette.secondaryText,
        topLeft = Offset(textLeft, center.y + line * 0.6f),
        size = Size(size.width * 0.28f, line),
        cornerRadius = CornerRadius(line / 2),
    )
}

@Composable
private fun BackgroundCard(background: FavoritesBackground, selected: Boolean, onClick: () -> Unit) {
    val label = stringResource(background.labelRes)
    val name = if (background.imageRes == null) {
        stringResource(R.string.background_none_description)
    } else {
        stringResource(R.string.background_description, label)
    }
    ChoiceColumn(
        label = label,
        description = if (selected) stringResource(R.string.choice_selected, name) else name,
        selected = selected,
        onClick = onClick,
        modifier = Modifier.width(CardWidth),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CARD_ASPECT_RATIO)
                .clip(CardShape)
                // The "none" card, and every card until its preview has been decoded.
                .background(RadioColors.BackgroundBrush),
        ) {
            background.imageRes?.let { imageRes ->
                rememberPreview(imageRes)?.let { preview ->
                    Image(
                        bitmap = preview,
                        // The card's own description already names it.
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = CardImageAlignment,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            ChoiceFrame(selected)
        }
    }
}

/**
 * One choice in the sheet: its [preview] with the label underneath. The whole
 * column is a single button, announced as [description].
 */
@Composable
private fun ChoiceColumn(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    preview: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        preview()
        Text(
            text = label,
            color = if (selected) RadioColors.GoldText else RadioColors.Text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier
                .padding(vertical = 6.dp)
                // Already part of the description.
                .clearAndSetSemantics {},
        )
    }
}

/** The outline of a preview, drawn over it so the picture never hides it, and the check mark of the current choice. */
@Composable
private fun BoxScope.ChoiceFrame(selected: Boolean) {
    Box(
        Modifier
            .matchParentSize()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) RadioColors.GoldText else RadioColors.Outline,
                shape = CardShape,
            ),
    )
    if (selected) {
        Canvas(
            Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
                .background(RadioColors.Gold, CircleShape),
        ) { drawCheckMark() }
    }
}

private fun DrawScope.drawCheckMark() {
    val check = Path().apply {
        moveTo(size.width * 0.28f, size.height * 0.52f)
        lineTo(size.width * 0.44f, size.height * 0.68f)
        lineTo(size.width * 0.73f, size.height * 0.35f)
    }
    drawPath(
        check,
        RadioColors.OnGold,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

/**
 * The image decoded at roughly card size, off the main thread, so the picker
 * never holds seven full-screen bitmaps. Null while decoding.
 */
@Composable
private fun rememberPreview(@DrawableRes imageRes: Int): ImageBitmap? {
    val resources = LocalResources.current
    val widthPx = with(LocalDensity.current) { CardWidth.roundToPx() }
    val preview by produceState<ImageBitmap?>(initialValue = null, imageRes, widthPx) {
        value = withContext(Dispatchers.Default) { decodeDownsampled(resources, imageRes, widthPx) }
    }
    return preview
}

private fun decodeDownsampled(resources: Resources, @DrawableRes imageRes: Int, minWidthPx: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(resources, imageRes, bounds)
    // The largest power of two that still leaves the image at least as wide as the card.
    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= minWidthPx) sampleSize *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return BitmapFactory.decodeResource(resources, imageRes, options)?.asImageBitmap()
}
