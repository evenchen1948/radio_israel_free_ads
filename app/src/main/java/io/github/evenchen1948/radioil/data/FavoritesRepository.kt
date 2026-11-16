package io.github.evenchen1948.radioil.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.favoritesStore by preferencesDataStore(name = "favorites")
private val FavoriteIds = stringSetPreferencesKey("station_ids")

/** The ids of the starred stations, kept on the device only. */
class FavoritesRepository(context: Context) {

    private val store = context.applicationContext.favoritesStore

    val favoriteIds: Flow<Set<String>> = store.data
        // An unreadable file means no favorites, not a crash.
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences -> preferences[FavoriteIds].orEmpty() }

    suspend fun toggle(stationId: String) {
        try {
            store.edit { preferences ->
                val current = preferences[FavoriteIds].orEmpty()
                preferences[FavoriteIds] = if (stationId in current) current - stationId else current + stationId
            }
        } catch (_: IOException) {
            // The star simply stays as it was.
        }
    }
}
