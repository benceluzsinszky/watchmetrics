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
    fun `delegates to TMDB client`() {
        given(tmdbClient.searchTv("succession")).willReturn(
            TmdbTvSearchResponse(results = listOf(TmdbTvShowSummary(id = 1, name = "Succession"))),
        )

        val response = seriesSearchService.search("succession")

        assertEquals("Succession", response.results.single().name)
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
