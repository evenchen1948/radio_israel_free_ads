package io.github.evenchen1948.radioil.data

import androidx.annotation.StringRes
import io.github.evenchen1948.radioil.R

/** The app's color themes. [id] is what gets saved, so it must never change for an entry. */
enum class AppTheme(val id: String, @StringRes val labelRes: Int) {
    DARK("dark", R.string.theme_dark),
    PEARL("pearl", R.string.theme_pearl);

    companion object {
        val Default = DARK

        /** The id of the silver theme, which the pearl theme replaced. */
        private const val LEGACY_SILVER_ID = "silver"

        /** Nothing saved yet, or an id this version does not know: the default. */
        fun fromId(id: String?): AppTheme =
            if (id == LEGACY_SILVER_ID) PEARL else entries.firstOrNull { it.id == id } ?: Default
    }
}
