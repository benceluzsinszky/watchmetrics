package com.watchmetrics.client

import com.watchmetrics.config.OmdbProperties
import com.watchmetrics.model.OmdbSeasonResponse
import com.watchmetrics.model.OmdbTitleDetail
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody
import org.springframework.web.util.UriBuilder
import java.net.URI

@Component
class OmdbClient(
    @Qualifier("omdbRestClient") private val omdbRestClient: RestClient,
    private val properties: OmdbProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getTitle(imdbId: String): OmdbTitleDetail? {
        if (!properties.isConfigured || imdbId.isBlank()) {
            return null
        }

        return runCatching {
            omdbRestClient.get()
                .uri { builder -> omdbUri(builder) { queryParam("i", imdbId) } }
                .retrieve()
                .requiredBody<OmdbTitleDetail>()
                .takeIf { it.isSuccessful }
        }.onFailure { ex ->
            log.warn("OMDb title lookup failed for {}: {}", imdbId, ex.message)
        }.getOrNull()
    }

    fun getSeason(imdbId: String, seasonNumber: Int): OmdbSeasonResponse? {
        if (!properties.isConfigured || imdbId.isBlank()) {
            return null
        }

        return runCatching {
            omdbRestClient.get()
                .uri { builder ->
                    omdbUri(builder) {
                        queryParam("i", imdbId)
                        queryParam("Season", seasonNumber)
                    }
                }
                .retrieve()
                .requiredBody<OmdbSeasonResponse>()
                .takeIf { it.isSuccessful }
        }.onFailure { ex ->
            log.warn("OMDb season lookup failed for {} season {}: {}", imdbId, seasonNumber, ex.message)
        }.getOrNull()
    }

    private fun omdbUri(builder: UriBuilder, configure: UriBuilder.() -> Unit): URI {
        builder.path("/")
        builder.queryParam("apikey", properties.apiKey)
        builder.configure()
        return builder.build()
    }
}
