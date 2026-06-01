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
    val lastEpisodeNumber: Int,
    val lastEpisodeName: String,
    val lastEpisodeRating: Double?,
)

data class RatedSeasonHighlight(
    val seasonNumber: Int,
    val seasonName: String,
    val averageRating: Double,
) {
    val shortLabel: String
        get() = "S$seasonNumber"
}

data class RatedEpisodeHighlight(
    val seasonNumber: Int,
    val seasonName: String,
    val episodeNumber: Int,
    val episodeName: String,
    val imdbRating: Double,
) {
    val code: String
        get() = "S${seasonNumber}E$episodeNumber"
}

object SeriesHighlightsBuilder {
    fun build(seasons: List<SeasonView>): SeriesHighlightsView {
        val regularSeasons = seasons.filter { it.number != 0 }
        val lastSeason = regularSeasons.maxByOrNull { it.number }

        val finale = lastSeason?.let { season ->
            val lastEpisode = season.episodes.maxByOrNull { it.number } ?: return@let null
            SeriesFinaleHighlight(
                lastSeasonNumber = season.number,
                lastSeasonName = season.name,
                lastSeasonAverage = seasonAverage(season),
                lastEpisodeNumber = lastEpisode.number,
                lastEpisodeName = lastEpisode.name,
                lastEpisodeRating = lastEpisode.imdbRating?.takeIf { it > 0 },
            )
        }

        val seasonAverages = regularSeasons.mapNotNull { season ->
            seasonAverage(season)?.let { average ->
                RatedSeasonHighlight(
                    seasonNumber = season.number,
                    seasonName = season.name,
                    averageRating = average,
                )
            }
        }

        val ratedEpisodes = regularSeasons.flatMap { season ->
            season.episodes.mapNotNull { episode ->
                val rating = episode.imdbRating?.takeIf { it > 0 } ?: return@mapNotNull null
                RatedEpisodeHighlight(
                    seasonNumber = season.number,
                    seasonName = season.name,
                    episodeNumber = episode.number,
                    episodeName = episode.name,
                    imdbRating = rating,
                )
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

    private fun seasonAverage(season: SeasonView): Double? {
        val ratings = season.episodes.mapNotNull { episode ->
            episode.imdbRating?.takeIf { it > 0 }
        }
        return ratings.takeIf { it.isNotEmpty() }?.average()
    }

    private fun seasonHighlightComparator(): Comparator<RatedSeasonHighlight> =
        compareBy<RatedSeasonHighlight> { it.averageRating }
            .thenBy { it.seasonNumber }

    private fun episodeHighlightComparator(): Comparator<RatedEpisodeHighlight> =
        compareBy<RatedEpisodeHighlight> { it.imdbRating }
            .thenBy { it.seasonNumber }
            .thenBy { it.episodeNumber }
}
