package io.github.evenchen1948.radioil.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
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
private val FavoritesBackgroundBright = booleanPreferencesKey("favorites_background_bright")

/** The user's display choices, kept on the device only. */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.settingsStore

    val favoritesBackground: Flow<FavoritesBackground> = store.data
        // An unreadable file means the default background, not a crash.
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences -> FavoritesBackground.fromId(preferences[FavoritesBackgroundId]) }

    /** Whether the chosen illustration is shown under the lighter scrim. */
    val favoritesBackgroundBright: Flow<Boolean> = store.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences -> preferences[FavoritesBackgroundBright] ?: false }

    suspend fun setFavoritesBackground(background: FavoritesBackground) {
        try {
            store.edit { preferences -> preferences[FavoritesBackgroundId] = background.id }
        } catch (_: IOException) {
            // The background simply stays as it was.
        }
    }

    suspend fun setFavoritesBackgroundBright(bright: Boolean) {
        try {
            store.edit { preferences -> preferences[FavoritesBackgroundBright] = bright }
        } catch (_: IOException) {
            // The brightness simply stays as it was.
        }
    }
}
