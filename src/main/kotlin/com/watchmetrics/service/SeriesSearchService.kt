package com.watchmetrics.service

import com.watchmetrics.client.TmdbClient
import com.watchmetrics.model.TmdbTvSearchResponse
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException

@Service
class SeriesSearchService(
    private val tmdbClient: TmdbClient,
) {

    fun search(query: String): TmdbTvSearchResponse {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return TmdbTvSearchResponse()
        }

        return try {
            tmdbClient.searchTv(trimmed)
        } catch (ex: RestClientException) {
            throw SeriesSearchException("Could not search TMDB right now. Try again in a moment.", ex)
        } catch (ex: IllegalArgumentException) {
            throw SeriesSearchException(ex.message ?: "Invalid search request.", ex)
        }
    }
}

class SeriesSearchException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
