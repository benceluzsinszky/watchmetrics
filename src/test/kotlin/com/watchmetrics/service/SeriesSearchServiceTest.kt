package com.watchmetrics.service

import com.watchmetrics.client.TmdbClient
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class SeriesSearchServiceTest {

    @Mock
    private lateinit var tmdbClient: TmdbClient

    @InjectMocks
    private lateinit var seriesSearchService: SeriesSearchService

    @Test
    fun `blank query returns empty response without calling TMDB`() {
        val response = seriesSearchService.search("   ")

        assertTrue(response.results.isEmpty())
    }

    @Test
    fun `returns TMDB results without blocking on finale verdict`() {
        given(tmdbClient.searchTv("breaking")).willReturn(
            TmdbTvSearchResponse(
                results = (1..9).map { id ->
                    TmdbTvShowSummary(id = id, name = "Show $id")
                },
                totalResults = 9,
            ),
        )

        val response = seriesSearchService.search("breaking")

        assertEquals(9, response.results.size)
        assertTrue(response.results[0].loadVerdict)
        assertTrue(response.results[7].loadVerdict)
        assertFalse(response.results[8].loadVerdict)
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
}
