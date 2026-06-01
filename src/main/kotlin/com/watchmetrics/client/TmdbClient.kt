package com.watchmetrics.client

import com.watchmetrics.config.TmdbProperties
import com.watchmetrics.model.TmdbTvSearchResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody

@Component
class TmdbClient(
    private val tmdbRestClient: RestClient,
    private val properties: TmdbProperties,
) {

    fun searchTv(query: String, page: Int = 1): TmdbTvSearchResponse {
        require(properties.isConfigured) {
            "TMDB is not configured. Set TMDB_ACCESS_TOKEN or TMDB_API_KEY in your environment."
        }
        require(query.isNotBlank()) { "Search query must not be blank." }

        return tmdbRestClient.get()
            .uri { builder ->
                builder.path("/search/tv")
                    .queryParam("query", query.trim())
                    .queryParam("page", page)
                    .apply {
                        if (properties.accessToken.isBlank() && properties.apiKey.isNotBlank()) {
                            queryParam("api_key", properties.apiKey)
                        }
                    }
                    .build()
            }
            .retrieve()
            .requiredBody<TmdbTvSearchResponse>()
    }
}
