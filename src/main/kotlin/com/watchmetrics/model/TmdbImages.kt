package com.watchmetrics.model

object TmdbImages {
    fun posterUrl(path: String?, size: String = "w342"): String? =
        path?.let { "https://image.tmdb.org/t/p/$size$it" }
}
