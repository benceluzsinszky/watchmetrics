package com.watchmetrics.model

data class SeasonRatingChartView(
    val bars: List<SeasonRatingBar>,
    val linePolylinePoints: String?,
    val linePoints: List<SeasonRatingLinePoint>,
    val hasData: Boolean,
)

data class SeasonRatingLinePoint(
    val x: Double,
    val y: Double,
    val rating: Double,
    val label: String,
)

data class SeasonRatingBar(
    val seasonNumber: Int,
    val seasonLabel: String,
    val averageRating: Double?,
    val ratedEpisodeCount: Int,
    val totalEpisodeCount: Int,
    val barColor: String,
    val barHeightPercent: Double,
    val barHeightPx: Int,
) {
    val shortLabel: String
        get() = "S$seasonNumber"

    val hasAverage: Boolean
        get() = averageRating != null
}

object SeasonRatingChartBuilder {
    const val CHART_PLOT_HEIGHT_PX = 200

    fun build(seasons: List<SeasonView>): SeasonRatingChartView {
        val bars = seasons
            .filter { it.number != 0 }
            .map { season ->
                val ratings = season.episodes.mapNotNull { episode ->
                    episode.imdbRating?.takeIf { it > 0 }
                }
                val average = ratings.takeIf { it.isNotEmpty() }?.average()
                SeasonRatingBar(
                    seasonNumber = season.number,
                    seasonLabel = season.name,
                    averageRating = average,
                    ratedEpisodeCount = ratings.size,
                    totalEpisodeCount = season.episodes.size,
                    barColor = RatingColors.forImdb(average),
                    barHeightPercent = average?.let { (it / 10.0) * 100.0 } ?: 0.0,
                    barHeightPx = average?.let {
                        ((it / 10.0) * CHART_PLOT_HEIGHT_PX).toInt().coerceAtLeast(1)
                    } ?: 0,
                )
            }

        return SeasonRatingChartView(
            bars = bars,
            linePolylinePoints = linePolylinePoints(bars),
            linePoints = linePoints(bars),
            hasData = bars.any { it.hasAverage },
        )
    }

    private fun linePoints(bars: List<SeasonRatingBar>): List<SeasonRatingLinePoint> =
        bars.mapIndexedNotNull { index, bar ->
            val rating = bar.averageRating ?: return@mapIndexedNotNull null
            SeasonRatingLinePoint(
                x = barX(index, bars.size),
                y = barY(rating),
                rating = rating,
                label = bar.shortLabel,
            )
        }

    private fun linePolylinePoints(bars: List<SeasonRatingBar>): String? {
        val points = linePoints(bars)
        if (points.size < 2) {
            return null
        }

        return points.joinToString(" ") { "${it.x},${it.y}" }
    }

    private fun barX(index: Int, barCount: Int): Double {
        if (barCount <= 1) {
            return 50.0
        }
        val slotWidth = 100.0 / barCount
        return slotWidth * index + slotWidth / 2.0
    }

    private fun barY(rating: Double): Double =
        100.0 - (rating / 10.0) * 100.0
}
