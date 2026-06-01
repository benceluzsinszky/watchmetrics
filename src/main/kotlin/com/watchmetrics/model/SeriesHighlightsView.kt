package com.watchmetrics.model

data class SeriesHighlightsView(
    val finale: SeriesFinaleHighlight?,
    val bestSeason: RatedSeasonHighlight?,
    val worstSeason: RatedSeasonHighlight?,
    val bestEpisode: RatedEpisodeHighlight?,
    val worstEpisode: RatedEpisodeHighlight?,
) {
    val hasData: Boolean
        get() = finale != null || bestSeason != null || worstSeason != null ||
            bestEpisode != null || worstEpisode != null
}

data class SeriesFinaleHighlight(
    val lastSeasonNumber: Int,
    val lastSeasonName: String,
    val lastSeasonAverage: Double?,
    val lastSeasonAverageSource: RatingSource?,
    val lastEpisodeNumber: Int,
    val lastEpisodeName: String,
    val lastEpisodeRating: Double?,
    val lastEpisodeRatingSource: RatingSource?,
) {
    val lastSeasonAverageLabel: String
        get() = formatAverageLabel(lastSeasonAverage, lastSeasonAverageSource)

    val lastEpisodeRatingLabel: String
        get() = formatRatingLabel(lastEpisodeRating, lastEpisodeRatingSource)

    private fun formatAverageLabel(rating: Double?, source: RatingSource?): String =
        when {
            rating != null && source != null -> "${RatingFormat.label(rating, source)} avg"
            rating != null -> "★ ${RatingFormat.format(rating)} avg"
            else -> "—"
        }

    private fun formatRatingLabel(rating: Double?, source: RatingSource?): String =
        when {
            rating != null && source != null -> RatingFormat.label(rating, source)
            rating != null -> "★ ${RatingFormat.format(rating)}"
            else -> "—"
        }
}

data class RatedSeasonHighlight(
    val seasonNumber: Int,
    val seasonName: String,
    val averageRating: Double,
    val ratingSource: RatingSource?,
) {
    val shortLabel: String
        get() = "S$seasonNumber"

    val ratingLabel: String
        get() = when (ratingSource) {
            null -> "★ ${RatingFormat.format(averageRating)} avg"
            else -> "${RatingFormat.label(averageRating, ratingSource)} avg"
        }
}

data class RatedEpisodeHighlight(
    val seasonNumber: Int,
    val seasonName: String,
    val episodeNumber: Int,
    val episodeName: String,
    val rating: Double,
    val ratingSource: RatingSource,
) {
    val code: String
        get() = "S${seasonNumber}E$episodeNumber"

    val ratingLabel: String
        get() = RatingFormat.label(rating, ratingSource)
}

object SeriesHighlightsBuilder {
    fun build(seasons: List<SeasonView>): SeriesHighlightsView {
        val regularSeasons = seasons.filter { it.number != 0 }
        val lastSeason = regularSeasons.maxByOrNull { it.number }

        val finale = lastSeason?.let { season ->
            val lastEpisode = season.episodes.maxByOrNull { it.number } ?: return@let null
            val (seasonAverage, seasonSource) = seasonAverageWithSource(season)
            val episodeRating = lastEpisode.resolvedRating()
            SeriesFinaleHighlight(
                lastSeasonNumber = season.number,
                lastSeasonName = season.name,
                lastSeasonAverage = seasonAverage,
                lastSeasonAverageSource = seasonSource,
                lastEpisodeNumber = lastEpisode.number,
                lastEpisodeName = lastEpisode.name,
                lastEpisodeRating = episodeRating?.value,
                lastEpisodeRatingSource = episodeRating?.source,
            )
        }

        val seasonAverages = regularSeasons.mapNotNull { season ->
            val (average, source) = seasonAverageWithSource(season)
            average?.let {
                RatedSeasonHighlight(
                    seasonNumber = season.number,
                    seasonName = season.name,
                    averageRating = it,
                    ratingSource = source,
                )
            }
        }

        val ratedEpisodes = regularSeasons.flatMap { season ->
            season.episodes.mapNotNull { episode ->
                episode.resolvedRating()?.let { rating ->
                    RatedEpisodeHighlight(
                        seasonNumber = season.number,
                        seasonName = season.name,
                        episodeNumber = episode.number,
                        episodeName = episode.name,
                        rating = rating.value,
                        ratingSource = rating.source,
                    )
                }
            }
        }

        val bestSeason = seasonAverages.maxWithOrNull(seasonHighlightComparator())
        val worstSeason = seasonAverages.minWithOrNull(seasonHighlightComparator())

        val bestEpisode = ratedEpisodes.maxWithOrNull(episodeHighlightComparator())
        val worstEpisode = ratedEpisodes.minWithOrNull(episodeHighlightComparator())

        return SeriesHighlightsView(
            finale = finale,
            bestSeason = bestSeason,
            worstSeason = worstSeason,
            bestEpisode = bestEpisode,
            worstEpisode = worstEpisode,
        )
    }

    private fun seasonAverageWithSource(season: SeasonView): Pair<Double?, RatingSource?> {
        val ratings = season.episodes.mapNotNull { it.resolvedRating() }
        return RatingResolver.average(ratings)
    }

    private fun seasonHighlightComparator(): Comparator<RatedSeasonHighlight> =
        compareBy<RatedSeasonHighlight> { it.averageRating }
            .thenBy { it.seasonNumber }

    private fun episodeHighlightComparator(): Comparator<RatedEpisodeHighlight> =
        compareBy<RatedEpisodeHighlight> { it.rating }
            .thenBy { it.seasonNumber }
            .thenBy { it.episodeNumber }
}
