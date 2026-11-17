package io.github.evenchen1948.radioil.ui

import android.content.res.Resources
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.evenchen1948.radioil.R
import io.github.evenchen1948.radioil.data.FavoritesBackground
import io.github.evenchen1948.radioil.ui.theme.RadioColors
import io.github.evenchen1948.radioil.ui.theme.SerifFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val CardWidth = 120.dp

/**
 * Width over height of a card. Much shorter than the illustrations, so the
 * sheet leaves the upper half of the tab, and the change behind it, in view.
 */
private const val CARD_ASPECT_RATIO = 3f / 4f

/** Crops to the lower part of an illustration, where the radio stands, with a little floor under it. */
private val CardImageAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = 0.6f)
private val CardShape = RoundedCornerShape(14.dp)

/** The round button on the favorites tab that opens the background picker. */
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
 * Bottom sheet with one preview card per background. A tap applies the choice
 * at once and the sheet stays open, so several can be tried against the real
 * tab; for the same reason the sheet does not dim what is behind it. A switch
 * under the cards shows the chosen illustration under a lighter scrim.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundPickerSheet(
    selected: FavoritesBackground?,
    bright: Boolean,
    onSelect: (FavoritesBackground) -> Unit,
    onBrightChange: (Boolean) -> Unit,
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
    ) {
        Text(
            text = stringResource(R.string.background_picker_title),
            color = RadioColors.Text,
            fontSize = 20.sp,
            fontFamily = SerifFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .semantics { heading() },
        )
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
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
        // Brightness only means something while there is an illustration to brighten.
        AnimatedVisibility(visible = selected?.imageRes != null) {
            BrightnessSwitch(checked = bright, onCheckedChange = onBrightChange)
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** A full-width row that toggles as one control: the label, then the switch. */
@Composable
private fun BrightnessSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.background_brighter),
            color = RadioColors.Text,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            // The row handles the tap, so the row and the switch are one target.
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RadioColors.OnGold,
                checkedTrackColor = RadioColors.Gold,
                checkedBorderColor = RadioColors.Gold,
                uncheckedThumbColor = RadioColors.SecondaryText,
                uncheckedTrackColor = RadioColors.Background,
                uncheckedBorderColor = RadioColors.SecondaryText,
            ),
        )
    }
}

@Composable
private fun BackgroundCard(background: FavoritesBackground, selected: Boolean, onClick: () -> Unit) {
    val label = stringResource(background.labelRes)
    val name = if (background.imageRes == null) {
        stringResource(R.string.background_none_description)
    } else {
        stringResource(R.string.background_description, label)
    }
    val description = if (selected) stringResource(R.string.background_selected, name) else name

    Column(
        modifier = Modifier
            .width(CardWidth)
            .clip(CardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CARD_ASPECT_RATIO)
                .clip(CardShape)
                // The "none" card, and every card until its preview has been decoded.
                .background(RadioColors.Background),
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
            // Drawn over the picture so the outline is never hidden by it.
            Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) RadioColors.Gold else RadioColors.Outline,
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
        Text(
            text = label,
            color = if (selected) RadioColors.Gold else RadioColors.Text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier
                .padding(vertical = 10.dp)
                // Already part of the card's description.
                .clearAndSetSemantics {},
        )
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
