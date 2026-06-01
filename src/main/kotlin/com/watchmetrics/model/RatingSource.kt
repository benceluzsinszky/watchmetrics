package com.watchmetrics.model

import java.util.Locale

enum class RatingSource(val displayName: String) {
    IMDB("IMDb"),
    TMDB("TMDB"),
}

data class DisplayRating(
    val value: Double,
    val source: RatingSource,
) {
    val formatted: String
        get() = RatingFormat.label(value, source)
}

object RatingFormat {
    fun format(value: Double): String =
        "%.1f".format(Locale.US, value)

    fun label(value: Double, source: RatingSource): String =
        "★ ${format(value)} ${source.displayName}"
}

object RatingResolver {
    fun from(imdb: Double?, tmdb: Double?): DisplayRating? {
        imdb?.takeIf { it > 0 }?.let { return DisplayRating(it, RatingSource.IMDB) }
        tmdb?.takeIf { it > 0 }?.let { return DisplayRating(it, RatingSource.TMDB) }
        return null
    }

    fun average(ratings: List<DisplayRating>): Pair<Double?, RatingSource?> {
        if (ratings.isEmpty()) {
            return null to null
        }
        val avg = ratings.map { it.value }.average()
        val sources = ratings.map { it.source }.toSet()
        val source = when {
            sources.size == 1 -> sources.single()
            else -> null
        }
        return avg to source
    }
}
