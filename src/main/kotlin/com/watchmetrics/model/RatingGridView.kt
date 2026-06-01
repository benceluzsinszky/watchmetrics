package com.watchmetrics.model

data class RatingGridView(
    val maxEpisodes: Int,
    val rows: List<RatingGridRow>,
)

data class RatingGridRow(
    val seasonLabel: String,
    val cells: List<RatingGridCell>,
)

data class RatingGridCell(
    val seasonLabel: String,
    val episodeNumber: Int?,
    val episodeName: String?,
    val imdbRating: Double?,
    val heatmapColor: String,
) {
    val hasEpisode: Boolean
        get() = episodeNumber != null

    val hasRating: Boolean
        get() = imdbRating != null && imdbRating > 0

    val tooltip: String?
        get() = when {
            !hasEpisode -> null
            hasRating -> "$seasonLabel · E$episodeNumber $episodeName — ★ ${"%.1f".format(imdbRating)} IMDb"
            else -> "$seasonLabel · E$episodeNumber $episodeName — no rating"
        }
}
