package com.watchmetrics.service

import com.watchmetrics.client.TmdbClient
import com.watchmetrics.model.FinaleVerdictEvaluator
import com.watchmetrics.model.FinaleVerdictResult
import com.watchmetrics.model.SeriesSearchPageView
import com.watchmetrics.model.SeriesSearchResultView
import com.watchmetrics.model.TmdbTvSearchResponse
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException

@Service
class SeriesSearchService(
    private val tmdbClient: TmdbClient,
    private val seriesDetailService: SeriesDetailService,
) {

    fun search(query: String): SeriesSearchPageView {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return SeriesSearchPageView(results = emptyList(), totalResults = 0)
        }

        return try {
            val response = tmdbClient.searchTv(trimmed)
            SeriesSearchPageView(
                results = enrichResults(response),
                totalResults = response.totalResults,
            )
        } catch (ex: RestClientException) {
            throw SeriesSearchException("Could not search TMDB right now. Try again in a moment.", ex)
        } catch (ex: IllegalArgumentException) {
            throw SeriesSearchException(ex.message ?: "Invalid search request.", ex)
        }
    }

    private fun enrichResults(response: TmdbTvSearchResponse): List<SeriesSearchResultView> =
        response.results.mapIndexed { index, summary ->
            val verdict = if (index < MAX_FINALE_LOOKUPS) {
                resolveFinaleVerdict(summary.id)
            } else {
                null
            }
            SeriesSearchResultView.from(summary, verdict)
        }

    private fun resolveFinaleVerdict(tmdbId: Int): FinaleVerdictResult? =
        try {
            val detail = seriesDetailService.getDetail(tmdbId)
            FinaleVerdictEvaluator.evaluate(detail.status, detail.highlights)
        } catch (_: SeriesNotFoundException) {
            null
        } catch (_: SeriesDetailException) {
            null
        }

    companion object {
        const val MAX_FINALE_LOOKUPS = 8
    }
}

class SeriesSearchException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
