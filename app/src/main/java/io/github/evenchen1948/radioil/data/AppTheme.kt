package io.github.evenchen1948.radioil.data

import androidx.annotation.StringRes
import io.github.evenchen1948.radioil.R

/** The app's color themes. [id] is what gets saved, so it must never change for an entry. */
enum class AppTheme(val id: String, @StringRes val labelRes: Int) {
    DARK("dark", R.string.theme_dark),
    SILVER("silver", R.string.theme_silver);

    companion object {
        val Default = DARK

        /** Nothing saved yet, or an id this version does not know: the default. */
        fun fromId(id: String?): AppTheme = entries.firstOrNull { it.id == id } ?: Default
    }
}
