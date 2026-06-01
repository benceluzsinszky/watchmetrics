package com.watchmetrics.model

object RatingColors {
    private const val EMPTY = "#1e293b"

    fun forImdb(rating: Double?): String {
        if (rating == null || rating <= 0) {
            return EMPTY
        }

        val hue = ((rating - 1.0) / 9.0 * 120.0).coerceIn(0.0, 120.0)
        return "hsl(${hue.toInt()}, 70%, 38%)"
    }
}
