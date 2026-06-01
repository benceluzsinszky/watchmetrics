package com.watchmetrics.service

import com.watchmetrics.client.TmdbClient
import com.watchmetrics.model.SeriesSearchPageView
import com.watchmetrics.model.SeriesSearchResultView
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException

@Service
class SeriesSearchService(
    private val tmdbClient: TmdbClient,
) {

    fun search(query: String): SeriesSearchPageView {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return SeriesSearchPageView(results = emptyList(), totalResults = 0)
        }

        return try {
            val response = tmdbClient.searchTv(trimmed)
            SeriesSearchPageView(
                results = response.results.mapIndexed { index, summary ->
                    SeriesSearchResultView.from(
                        summary = summary,
                        loadVerdict = index < MAX_VERDICT_BADGES,
                    )
                },
                totalResults = response.totalResults,
            )
        } catch (ex: RestClientException) {
            throw SeriesSearchException("Could not search TMDB right now. Try again in a moment.", ex)
        } catch (ex: IllegalArgumentException) {
            throw SeriesSearchException(ex.message ?: "Invalid search request.", ex)
        }
    }

    companion object {
        const val MAX_VERDICT_BADGES = 8
    }
}

class SeriesSearchException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
