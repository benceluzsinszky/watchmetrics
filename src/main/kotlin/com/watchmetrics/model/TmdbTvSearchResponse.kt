package com.watchmetrics.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvSearchResponse(
    val page: Int = 0,
    val results: List<TmdbTvShowSummary> = emptyList(),
    @JsonProperty("total_pages")
    val totalPages: Int = 0,
    @JsonProperty("total_results")
    val totalResults: Int = 0,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvShowSummary(
    val id: Int,
    val name: String,
    @JsonProperty("original_name")
    val originalName: String? = null,
    val overview: String? = null,
    @JsonProperty("poster_path")
    val posterPath: String? = null,
    @JsonProperty("first_air_date")
    val firstAirDate: String? = null,
    @JsonProperty("vote_average")
    val voteAverage: Double? = null,
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w92$it" }
}
