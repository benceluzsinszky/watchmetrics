package com.watchmetrics.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "omdb")
data class OmdbProperties(
    val apiKey: String = "",
    val baseUrl: String = "https://www.omdbapi.com",
) {
    val isConfigured: Boolean
        get() = apiKey.isNotBlank()
}
