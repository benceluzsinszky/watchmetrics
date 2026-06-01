package com.watchmetrics.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvShowDetail(
    val id: Int,
    val name: String,
    val overview: String? = null,
    @JsonProperty("poster_path")
    val posterPath: String? = null,
    @JsonProperty("first_air_date")
    val firstAirDate: String? = null,
    @JsonProperty("vote_average")
    val voteAverage: Double? = null,
    val seasons: List<TmdbSeasonRef> = emptyList(),
) {
    val posterUrl: String?
        get() = TmdbImages.posterUrl(posterPath, "w342")
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbSeasonRef(
    @JsonProperty("season_number")
    val seasonNumber: Int,
    val name: String? = null,
    @JsonProperty("episode_count")
    val episodeCount: Int = 0,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbSeasonDetail(
    @JsonProperty("season_number")
    val seasonNumber: Int,
    val name: String? = null,
    val episodes: List<TmdbEpisode> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbEpisode(
    @JsonProperty("episode_number")
    val episodeNumber: Int,
    val name: String,
    val overview: String? = null,
    @JsonProperty("vote_average")
    val voteAverage: Double? = null,
    @JsonProperty("air_date")
    val airDate: String? = null,
    @JsonProperty("still_path")
    val stillPath: String? = null,
) {
    val stillUrl: String?
        get() = TmdbImages.posterUrl(stillPath, "w185")

    val hasRating: Boolean
        get() = voteAverage != null && voteAverage > 0
}
