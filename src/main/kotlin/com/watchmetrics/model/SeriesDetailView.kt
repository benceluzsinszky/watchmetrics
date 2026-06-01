package com.watchmetrics.model

data class SeriesDetailView(
    val id: Int,
    val name: String,
    val overview: String?,
    val posterUrl: String?,
    val firstAirYear: String?,
    val voteAverage: Double?,
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
    val rating: Double?,
    val airDate: String?,
) {
    val hasRating: Boolean
        get() = rating != null && rating > 0
}
