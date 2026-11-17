package io.github.evenchen1948.radioil.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.evenchen1948.radioil.R

/**
 * What the user can choose to show behind the favorites tab: a radio illustration
 * per decade, or nothing. [id] is what gets saved, so it must never change for an entry.
 */
enum class FavoritesBackground(
    val id: String,
    /** Null for the plain dark background. */
    @DrawableRes val imageRes: Int?,
    @StringRes val labelRes: Int,
) {
    NONE("none", null, R.string.background_none),
    DECADE_1920S("1920s", R.drawable.radio_1920s_bg, R.string.background_1920s),
    DECADE_1930S("1930s", R.drawable.radio_1930s_bg, R.string.background_1930s),
    DECADE_1940S("1940s", R.drawable.radio_1940s_bg, R.string.background_1940s),
    DECADE_1950S("1950s", R.drawable.radio_1950s_bg, R.string.background_1950s),
    DECADE_1960S("1960s", R.drawable.radio_1960s_bg, R.string.background_1960s),
    DECADE_1970S("1970s", R.drawable.radio_1970s_bg, R.string.background_1970s),
    DECADE_1980S("1980s", R.drawable.radio_1980s_bg, R.string.background_1980s);

    companion object {
        val Default = DECADE_1920S

        /** Nothing saved yet, or an id this version does not know: the default. */
        fun fromId(id: String?): FavoritesBackground = entries.firstOrNull { it.id == id } ?: Default
    }
}
