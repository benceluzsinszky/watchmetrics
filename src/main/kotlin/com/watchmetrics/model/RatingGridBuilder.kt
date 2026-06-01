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
            RatingGridRow(
                seasonLabel = season.name,
                cells = (1..maxEpisodes).map { episodeNumber ->
                    val episode = episodesByNumber[episodeNumber]
                    if (episode == null) {
                        RatingGridCell(
                            episodeNumber = null,
                            episodeName = null,
                            imdbRating = null,
                            heatmapColor = RatingColors.forImdb(null),
                            seasonLabel = season.name,
                        )
                    } else {
                        RatingGridCell(
                            episodeNumber = episode.number,
                            episodeName = episode.name,
                            imdbRating = episode.imdbRating,
                            heatmapColor = RatingColors.forImdb(episode.imdbRating),
                            seasonLabel = season.name,
                        )
                    }
                },
            )
        }

        return RatingGridView(maxEpisodes = maxEpisodes, rows = rows)
    }

    private fun List<SeasonView>.maxEpisodeNumber(): Int =
        maxOfOrNull { season -> season.episodes.maxOfOrNull { it.number } ?: 0 } ?: 0
}
