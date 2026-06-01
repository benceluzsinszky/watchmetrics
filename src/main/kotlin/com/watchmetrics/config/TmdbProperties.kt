package com.watchmetrics.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tmdb")
data class TmdbProperties(
    val apiKey: String = "",
    val accessToken: String = "",
    val baseUrl: String = "https://api.themoviedb.org/3",
) {
    val isConfigured: Boolean
        get() = accessToken.isNotBlank() || apiKey.isNotBlank()
}
