package io.github.evenchen1948.radioil.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StationCategory {
    @SerialName("news") NEWS,
    @SerialName("music") MUSIC,
    @SerialName("talk") TALK,
    @SerialName("religious") RELIGIOUS,
    @SerialName("regional") REGIONAL,
}

@Serializable
enum class StreamType {
    @SerialName("hls") HLS,
    @SerialName("mp3") MP3,
}

/** One entry of assets/stations.json. */
@Serializable
data class Station(
    val id: String,
    val name: String,
    val category: StationCategory,
    val streamUrl: String,
    val streamType: StreamType,
    val logo: String,
)
