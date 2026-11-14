package com.israeliradio.app.data

import android.content.Context
import kotlinx.serialization.json.Json

/** Reads the bundled station list. stations.json is the only source of station data. */
class StationRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadStations(): List<Station> =
        context.assets.open(STATIONS_FILE).bufferedReader().use { reader ->
            json.decodeFromString<List<Station>>(reader.readText())
        }

    private companion object {
        const val STATIONS_FILE = "stations.json"
    }
}
