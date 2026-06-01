package com.watchmetrics.model

data class SeriesDetailView(
    val id: Int,
    val name: String,
    val overview: String?,
    val posterUrl: String?,
    val firstAirYear: String?,
    val imdbRating: Double?,
    val rottenTomatoesScore: Int?,
    val metacriticScore: Int?,
    val seasons: List<SeasonView>,
)

data class SeasonView(
    val number: Int,
    val name: String,
    val episodes: List<EpisodeView>,
)

data class EpisodeView(
    val number: Int,
    val name: String,
    val overview: String?,
    val imdbRating: Double?,
    val airDate: String?,
) {
    val hasImdbRating: Boolean
        get() = imdbRating != null && imdbRating > 0
}
