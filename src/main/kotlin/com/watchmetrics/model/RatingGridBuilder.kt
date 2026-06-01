package com.watchmetrics.model

object RatingGridBuilder {
    fun build(seasons: List<SeasonView>): RatingGridView {
        val seasonsForGrid = seasons.filter { it.number != 0 }
        val maxEpisodes = seasonsForGrid.maxEpisodeNumber()
        if (maxEpisodes == 0) {
            return RatingGridView(maxEpisodes = 0, rows = emptyList())
        }

        val rows = seasonsForGrid.map { season ->
            val episodesByNumber = season.episodes.associateBy { it.number }
            val seasonLabel = shortSeasonLabel(season.number)
            RatingGridRow(
                seasonLabel = seasonLabel,
                cells = (1..maxEpisodes).map { episodeNumber ->
                    val episode = episodesByNumber[episodeNumber]
                    if (episode == null) {
                        RatingGridCell(
                            seasonLabel = seasonLabel,
                            episodeNumber = null,
                            episodeName = null,
                            displayRating = null,
                            ratingSource = null,
                            heatmapColor = RatingColors.forImdb(null),
                        )
                    } else {
                        val rating = episode.resolvedRating()
                        RatingGridCell(
                            seasonLabel = seasonLabel,
                            episodeNumber = episode.number,
                            episodeName = episode.name,
                            displayRating = rating?.value,
                            ratingSource = rating?.source,
                            heatmapColor = RatingColors.forImdb(rating?.value),
                        )
                    }
                },
            )
        }

        return RatingGridView(maxEpisodes = maxEpisodes, rows = rows)
    }

    private fun shortSeasonLabel(seasonNumber: Int): String = "S$seasonNumber"

    private fun List<SeasonView>.maxEpisodeNumber(): Int =
        maxOfOrNull { season -> season.episodes.maxOfOrNull { it.number } ?: 0 } ?: 0
}
