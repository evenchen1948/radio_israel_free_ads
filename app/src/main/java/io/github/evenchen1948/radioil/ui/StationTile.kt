package io.github.evenchen1948.radioil.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.github.evenchen1948.radioil.data.Station
import io.github.evenchen1948.radioil.ui.theme.RadioColors
import io.github.evenchen1948.radioil.ui.theme.SerifFamily

/** What a text tile shows in place of a logo. */
data class TileLabel(val main: String, val caption: String?)

private val NumberInName = Regex("""\d+(\.\d+)?""")
private val FmSuffix = Regex("fm", RegexOption.IGNORE_CASE)

/**
 * A name with a number shows the number large and the rest of the name as a
 * caption ("כאן 88" -> "88" over "כאן"); a name without one is shown whole.
 */
fun Station.tileLabel(): TileLabel {
    val number = NumberInName.find(name)?.value ?: return TileLabel(main = name, caption = null)
    val rest = name.replaceFirst(number, " ").replace(FmSuffix, " ").trim().replace(Regex("""\s+"""), " ")
    return TileLabel(main = number, caption = rest.ifEmpty { "FM" })
}

/**
 * The text content of a station tile, sized to fit its container. Decorative:
 * callers show or announce the full station name next to it.
 */
@Composable
fun StationTileLabel(
    station: Station,
    mainColor: Color,
    maxMainSize: TextUnit,
    modifier: Modifier = Modifier,
    captionSize: TextUnit = 11.sp,
    captionGap: Dp = 2.dp,
) {
    val label = station.tileLabel()
    Column(
        modifier = modifier.clearAndSetSemantics { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText(
            text = label.main,
            style = TextStyle(
                color = mainColor,
                fontFamily = SerifFamily,
                fontWeight = if (label.caption != null) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            maxLines = if (label.caption != null) 1 else 2,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 12.sp,
                // A whole name reads better a little smaller than a lone number.
                maxFontSize = if (label.caption != null) maxMainSize else maxMainSize * 0.5f,
                stepSize = 1.sp,
            ),
        )
        if (label.caption != null) {
            BasicText(
                text = label.caption,
                style = TextStyle(
                    color = RadioColors.SecondaryText,
                    fontSize = captionSize,
                    letterSpacing = captionSize * 0.2f,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = captionGap),
            )
        }
    }
}
