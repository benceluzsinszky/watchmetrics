package com.watchmetrics.model

import java.util.Locale

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
    val displayRating: Double?,
    val ratingSource: RatingSource?,
    val heatmapColor: String,
) {
    val hasEpisode: Boolean
        get() = episodeNumber != null

    val hasRating: Boolean
        get() = displayRating != null && displayRating > 0

    val tooltip: String?
        get() = when {
            !hasEpisode -> null
            hasRating && ratingSource != null ->
                "$seasonLabel · E$episodeNumber $episodeName — ${RatingFormat.label(displayRating!!, ratingSource)}"
            hasRating ->
                "$seasonLabel · E$episodeNumber $episodeName — ★ ${RatingFormat.format(displayRating!!)}"
            else -> "$seasonLabel · E$episodeNumber $episodeName — no rating"
        }
}
