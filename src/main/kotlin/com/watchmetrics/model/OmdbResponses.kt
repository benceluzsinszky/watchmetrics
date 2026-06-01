package com.watchmetrics.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbExternalIds(
    @JsonProperty("imdb_id")
    val imdbId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OmdbSeasonResponse(
    val season: String? = null,
    @JsonProperty("Episodes")
    val episodes: List<OmdbEpisodeSummary> = emptyList(),
    @JsonProperty("Response")
    val response: String? = null,
) {
    val isSuccessful: Boolean
        get() = response.equals("True", ignoreCase = true)
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class OmdbEpisodeSummary(
    @JsonProperty("Title")
    val title: String? = null,
    @JsonProperty("Episode")
    val episode: String? = null,
    @JsonProperty("imdbRating")
    val imdbRating: String? = null,
    @JsonProperty("Released")
    val released: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OmdbTitleDetail(
    @JsonProperty("imdbRating")
    val imdbRating: String? = null,
    @JsonProperty("Metascore")
    val metascore: String? = null,
    @JsonProperty("Ratings")
    val ratings: List<OmdbRating> = emptyList(),
    @JsonProperty("Response")
    val response: String? = null,
) {
    val isSuccessful: Boolean
        get() = response.equals("True", ignoreCase = true)

    val rottenTomatoesScore: Int?
        get() = ratings.find { it.source == "Rotten Tomatoes" }?.value?.removeSuffix("%")?.toIntOrNull()

    val metacriticScore: Int?
        get() = metascore?.takeIf { it != "N/A" }?.toIntOrNull()
            ?: ratings.find { it.source == "Metacritic" }?.value?.split("/")?.firstOrNull()?.toIntOrNull()
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class OmdbRating(
    val source: String? = null,
    val value: String? = null,
)

object OmdbParsing {
    fun parseRating(value: String?): Double? =
        value?.trim()?.takeIf { it.isNotEmpty() && !it.equals("N/A", ignoreCase = true) }?.toDoubleOrNull()

    fun parseEpisodeNumber(value: String?): Int? =
        value?.trim()?.toIntOrNull()
}
