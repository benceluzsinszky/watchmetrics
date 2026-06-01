package com.watchmetrics.service

import com.watchmetrics.client.TmdbClient
import com.watchmetrics.model.FinaleVerdict
import com.watchmetrics.model.FinaleVerdictResult
import com.watchmetrics.model.SeriesDetailView
import com.watchmetrics.model.SeriesHighlightsView
import com.watchmetrics.model.SeriesFinaleHighlight
import com.watchmetrics.model.RatingGridView
import com.watchmetrics.model.SeasonRatingChartView
import com.watchmetrics.model.TmdbTvSearchResponse
import com.watchmetrics.model.TmdbTvShowSummary
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestClientResponseException
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class SeriesSearchServiceTest {

    @Mock
    private lateinit var tmdbClient: TmdbClient

    @Mock
    private lateinit var seriesDetailService: SeriesDetailService

    @InjectMocks
    private lateinit var seriesSearchService: SeriesSearchService

    @Test
    fun `blank query returns empty response without calling TMDB`() {
        val response = seriesSearchService.search("   ")

        assertTrue(response.results.isEmpty())
    }

    @Test
    fun `enriches search results with finale verdict`() {
        given(tmdbClient.searchTv("game of thrones")).willReturn(
            TmdbTvSearchResponse(
                results = listOf(TmdbTvShowSummary(id = 1399, name = "Game of Thrones")),
                totalResults = 1,
            ),
        )
        given(seriesDetailService.getDetail(1399)).willReturn(endedShowDetail(6.4))

        val response = seriesSearchService.search("game of thrones")

        assertEquals("Game of Thrones", response.results.single().name)
        assertEquals(FinaleVerdict.ENDED_BADLY, response.results.single().finaleVerdict?.verdict)
    }

    @Test
    fun `continues search when finale lookup fails`() {
        given(tmdbClient.searchTv("mystery")).willReturn(
            TmdbTvSearchResponse(
                results = listOf(TmdbTvShowSummary(id = 999, name = "Mystery Show")),
                totalResults = 1,
            ),
        )
        given(seriesDetailService.getDetail(999)).willThrow(SeriesNotFoundException(999))

        val response = seriesSearchService.search("mystery")

        assertNull(response.results.single().finaleVerdict)
    }

    @Test
    fun `wraps TMDB errors`() {
        given(tmdbClient.searchTv("fail")).willThrow(
            RestClientResponseException("error", 500, "Internal Server Error", null, null, null),
        )

        assertThrows<SeriesSearchException> {
            seriesSearchService.search("fail")
        }
    }

    private fun endedShowDetail(finaleRating: Double): SeriesDetailView =
        SeriesDetailView(
            id = 1399,
            name = "Game of Thrones",
            overview = null,
            posterUrl = null,
            firstAirYear = "2011",
            status = "Ended",
            imdbRating = 9.0,
            rottenTomatoesScore = null,
            metacriticScore = null,
            seasons = emptyList(),
            ratingGrid = RatingGridView(maxEpisodes = 0, rows = emptyList()),
            seasonRatingChart = SeasonRatingChartView(
                bars = emptyList(),
                linePolylinePoints = null,
                linePoints = emptyList(),
                hasData = false,
            ),
            highlights = SeriesHighlightsView(
                finale = SeriesFinaleHighlight(
                    lastSeasonNumber = 8,
                    lastSeasonName = "Season 8",
                    lastSeasonAverage = finaleRating,
                    lastEpisodeNumber = 6,
                    lastEpisodeName = "The Iron Throne",
                    lastEpisodeRating = finaleRating,
                ),
                bestSeason = null,
                worstSeason = null,
                bestEpisode = null,
                worstEpisode = null,
            ),
        )
}
