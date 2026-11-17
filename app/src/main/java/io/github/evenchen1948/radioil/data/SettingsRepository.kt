package io.github.evenchen1948.radioil.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsStore by preferencesDataStore(name = "settings")
private val FavoritesBackgroundId = stringPreferencesKey("favorites_background")
private val ThemeId = stringPreferencesKey("theme")

/** The user's display choices, kept on the device only. */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.settingsStore

    // An unreadable file means the defaults, not a crash.
    private val preferences = store.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }

    val favoritesBackground: Flow<FavoritesBackground> =
        preferences.map { FavoritesBackground.fromId(it[FavoritesBackgroundId]) }

    val theme: Flow<AppTheme> = preferences.map { AppTheme.fromId(it[ThemeId]) }

    suspend fun setFavoritesBackground(background: FavoritesBackground) {
        try {
            store.edit { it[FavoritesBackgroundId] = background.id }
        } catch (_: IOException) {
            // The background simply stays as it was.
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        try {
            store.edit { it[ThemeId] = theme.id }
        } catch (_: IOException) {
            // The theme simply stays as it was.
        }
    }
}
