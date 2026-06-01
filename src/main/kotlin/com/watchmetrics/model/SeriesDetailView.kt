package com.watchmetrics.model

data class SeriesDetailView(
    val id: Int,
    val name: String,
    val overview: String?,
    val posterUrl: String?,
    val firstAirYear: String?,
    val status: String?,
    val displayRating: Double?,
    val ratingSource: RatingSource?,
    val rottenTomatoesScore: Int?,
    val metacriticScore: Int?,
    val seasons: List<SeasonView>,
    val ratingGrid: RatingGridView,
    val seasonRatingChart: SeasonRatingChartView,
    val highlights: SeriesHighlightsView,
) {
    val showRatingLabel: String
        get() = when {
            displayRating != null && ratingSource != null ->
                RatingFormat.label(displayRating, ratingSource)
            else -> "—"
        }
}

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
    val tmdbRating: Double?,
    val airDate: String?,
) {
    val displayRating: Double?
        get() = resolvedRating()?.value

    val ratingSource: RatingSource?
        get() = resolvedRating()?.source

    val hasRating: Boolean
        get() = displayRating != null

    val ratingLabel: String
        get() = resolvedRating()?.formatted ?: "—"

    fun resolvedRating(): DisplayRating? =
        RatingResolver.from(imdbRating, tmdbRating)
}
