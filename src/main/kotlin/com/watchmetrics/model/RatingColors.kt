package com.watchmetrics.model

object RatingColors {
    private const val EMPTY = "#1e293b"

    fun forImdb(rating: Double?): String {
        if (rating == null || rating <= 0) {
            return EMPTY
        }

        return if (rating >= 5.0) {
            colorAtOrAboveFive(rating)
        } else {
            colorBelowFive(rating)
        }
    }

    private fun colorAtOrAboveFive(rating: Double): String {
        // 5 → red, 10 → green
        val t = ((rating - 5.0) / 5.0).coerceIn(0.0, 1.0)
        val hue = (t * 120.0).toInt()
        val saturation = (72 + t * 8).toInt()
        val lightness = (36 + t * 6).toInt()
        return "hsl($hue, $saturation%, $lightness%)"
    }

    private fun colorBelowFive(rating: Double): String {
        // 1 → toxic sludge, 5 → red
        val t = ((rating - 1.0) / 4.0).coerceIn(0.0, 1.0)
        return when {
            t >= 0.75 -> {
                val local = (t - 0.75) / 0.25
                val saturation = (78 + local * 5).toInt()
                val lightness = (26 + local * 10).toInt()
                "hsl(0, $saturation%, $lightness%)"
            }
            t >= 0.5 -> {
                val local = (t - 0.5) / 0.25
                val hue = (340 + local * 20).toInt()
                val saturation = (60 + local * 18).toInt()
                val lightness = (20 + local * 6).toInt()
                "hsl($hue, $saturation%, $lightness%)"
            }
            t >= 0.25 -> {
                val local = (t - 0.25) / 0.25
                val hue = (290 + local * 50).toInt()
                val saturation = (40 + local * 20).toInt()
                val lightness = (16 + local * 4).toInt()
                "hsl($hue, $saturation%, $lightness%)"
            }
            else -> {
                val local = t / 0.25
                val hue = (25 + local * 20).toInt()
                val saturation = (28 + local * 12).toInt()
                val lightness = (12 + local * 4).toInt()
                "hsl($hue, $saturation%, $lightness%)"
            }
        }
    }
}
