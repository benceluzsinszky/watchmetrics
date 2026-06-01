package com.watchmetrics.client

import com.watchmetrics.config.CacheNames
import com.watchmetrics.config.TmdbProperties
import org.springframework.cache.annotation.Cacheable
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

    @Cacheable(
        cacheNames = [CacheNames.TMDB_SEARCH],
        key = "#query.trim().toLowerCase() + '-' + #page",
    )
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

    @Cacheable(cacheNames = [CacheNames.TMDB_SHOW], key = "#id")
    fun getTvShow(id: Int): TmdbTvShowDetail {
        requireConfigured()

        return tmdbRestClient.get()
            .uri { builder -> authUri(builder, "/tv/$id") }
            .retrieve()
            .requiredBody<TmdbTvShowDetail>()
    }

    @Cacheable(
        cacheNames = [CacheNames.TMDB_SEASON],
        key = "#showId + '-' + #seasonNumber",
    )
    fun getTvSeason(showId: Int, seasonNumber: Int): TmdbSeasonDetail {
        requireConfigured()

        return tmdbRestClient.get()
            .uri { builder -> authUri(builder, "/tv/$showId/season/$seasonNumber") }
            .retrieve()
            .requiredBody<TmdbSeasonDetail>()
    }

    @Cacheable(cacheNames = [CacheNames.TMDB_EXTERNAL_IDS], key = "#id")
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
