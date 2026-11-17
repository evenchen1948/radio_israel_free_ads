package io.github.evenchen1948.radioil.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.evenchen1948.radioil.R
import io.github.evenchen1948.radioil.ui.theme.RadioColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Accessibility label of the favorite star, which names what a tap will do. */
@Composable
fun favoriteLabel(isFavorite: Boolean): String =
    stringResource(if (isFavorite) R.string.action_remove_favorite else R.string.action_add_favorite)

/**
 * The favorite star for a station tile: a 48dp touch target with the star
 * tucked into its top end corner. Gold when starred, outlined grey when not.
 */
@Composable
fun FavoriteToggle(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val label = favoriteLabel(isFavorite)
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onToggle)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.TopEnd,
    ) {
        Canvas(
            Modifier
                .padding(top = 10.dp, end = 10.dp)
                .size(18.dp),
        ) { drawStar(isFavorite) }
    }
}

/** A five-pointed star centred in the canvas. */
internal fun DrawScope.drawStar(filled: Boolean, radius: Float = size.minDimension / 2) {
    val path = Path()
    repeat(10) { point ->
        val r = if (point % 2 == 0) radius else radius * 0.42f
        val angle = -PI / 2 + point * PI / 5
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (point == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    val color = if (filled) RadioColors.GoldText else RadioColors.SecondaryText
    if (filled) drawPath(path, color)
    drawPath(path, color, style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round))
}
