package com.watchmetrics.service

import com.watchmetrics.client.TmdbClient
import com.watchmetrics.model.TmdbEpisode
import com.watchmetrics.model.TmdbSeasonDetail
import com.watchmetrics.model.TmdbSeasonRef
import com.watchmetrics.model.TmdbTvShowDetail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestClientResponseException
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SeriesDetailServiceTest {

    @Mock
    private lateinit var tmdbClient: TmdbClient

    @InjectMocks
    private lateinit var seriesDetailService: SeriesDetailService

    @Test
    fun `loads show with seasons and episode ratings`() {
        given(tmdbClient.getTvShow(1396)).willReturn(
            TmdbTvShowDetail(
                id = 1396,
                name = "Breaking Bad",
                overview = "A chemistry teacher.",
                posterPath = "/poster.jpg",
                firstAirDate = "2008-01-20",
                voteAverage = 8.9,
                seasons = listOf(
                    TmdbSeasonRef(seasonNumber = 1, name = "Season 1", episodeCount = 1),
                ),
            ),
        )
        given(tmdbClient.getTvSeason(1396, 1)).willReturn(
            TmdbSeasonDetail(
                seasonNumber = 1,
                name = "Season 1",
                episodes = listOf(
                    TmdbEpisode(
                        episodeNumber = 1,
                        name = "Pilot",
                        voteAverage = 8.5,
                        airDate = "2008-01-20",
                    ),
                ),
            ),
        )

        val detail = seriesDetailService.getDetail(1396)

        assertEquals("Breaking Bad", detail.name)
        assertEquals(1, detail.seasons.size)
        assertEquals(8.5, detail.seasons.single().episodes.single().rating)
    }

    @Test
    fun `maps 404 to not found`() {
        given(tmdbClient.getTvShow(999)).willThrow(
            RestClientResponseException("Not found", 404, "Not Found", null, null, null),
        )

        assertThrows<SeriesNotFoundException> {
            seriesDetailService.getDetail(999)
        }
    }
}
