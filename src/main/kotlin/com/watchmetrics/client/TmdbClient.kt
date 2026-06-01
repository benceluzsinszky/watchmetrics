package com.watchmetrics.client

import com.watchmetrics.config.TmdbProperties
import com.watchmetrics.model.TmdbExternalIds
import com.watchmetrics.model.TmdbSeasonDetail
import com.watchmetrics.model.TmdbTvSearchResponse
import com.watchmetrics.model.TmdbTvShowDetail
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody
import org.springframework.web.util.UriBuilder
import java.net.URI

@Component
class TmdbClient(
    @Qualifier("tmdbRestClient") private val tmdbRestClient: RestClient,
    private val properties: TmdbProperties,
) {

    fun searchTv(query: String, page: Int = 1): TmdbTvSearchResponse {
        requireConfigured()
        require(query.isNotBlank()) { "Search query must not be blank." }

        return tmdbRestClient.get()
            .uri { builder ->
                authUri(builder, "/search/tv") {
                    queryParam("query", query.trim())
                    queryParam("page", page)
                }
            }
            .retrieve()
            .requiredBody<TmdbTvSearchResponse>()
    }

    fun getTvShow(id: Int): TmdbTvShowDetail {
        requireConfigured()

        return tmdbRestClient.get()
            .uri { builder -> authUri(builder, "/tv/$id") }
            .retrieve()
            .requiredBody<TmdbTvShowDetail>()
    }

    fun getTvSeason(showId: Int, seasonNumber: Int): TmdbSeasonDetail {
        requireConfigured()

        return tmdbRestClient.get()
            .uri { builder -> authUri(builder, "/tv/$showId/season/$seasonNumber") }
            .retrieve()
            .requiredBody<TmdbSeasonDetail>()
    }

    fun getTvExternalIds(id: Int): TmdbExternalIds {
        requireConfigured()

        return tmdbRestClient.get()
            .uri { builder -> authUri(builder, "/tv/$id/external_ids") }
            .retrieve()
            .requiredBody<TmdbExternalIds>()
    }

    private fun requireConfigured() {
        require(properties.isConfigured) {
            "TMDB is not configured. Set TMDB_ACCESS_TOKEN or TMDB_API_KEY in your environment."
        }
    }

    private fun authUri(builder: UriBuilder, path: String, configure: UriBuilder.() -> Unit = {}): URI {
        builder.path(path)
        if (properties.accessToken.isBlank() && properties.apiKey.isNotBlank()) {
            builder.queryParam("api_key", properties.apiKey)
        }
        builder.configure()
        return builder.build()
    }
}
