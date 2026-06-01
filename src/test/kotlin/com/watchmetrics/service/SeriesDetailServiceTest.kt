package com.watchmetrics.service

import com.watchmetrics.client.OmdbClient
import com.watchmetrics.client.TmdbClient
import com.watchmetrics.model.OmdbEpisodeSummary
import com.watchmetrics.model.OmdbSeasonResponse
import com.watchmetrics.model.OmdbTitleDetail
import com.watchmetrics.model.TmdbEpisode
import com.watchmetrics.model.TmdbExternalIds
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

    @Mock
    private lateinit var omdbClient: OmdbClient

    @InjectMocks
    private lateinit var seriesDetailService: SeriesDetailService

    @Test
    fun `merges imdb episode ratings from omdb`() {
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
        given(tmdbClient.getTvExternalIds(1396)).willReturn(TmdbExternalIds(imdbId = "tt0903747"))
        given(omdbClient.getTitle("tt0903747")).willReturn(
            OmdbTitleDetail(imdbRating = "9.5", response = "True"),
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
        given(omdbClient.getSeason("tt0903747", 1)).willReturn(
            OmdbSeasonResponse(
                response = "True",
                episodes = listOf(
                    OmdbEpisodeSummary(episode = "1", imdbRating = "7.9"),
                ),
            ),
        )

        val detail = seriesDetailService.getDetail(1396)

        assertEquals("Breaking Bad", detail.name)
        assertEquals(9.5, detail.imdbRating)
        val episode = detail.seasons.single().episodes.single()
        assertEquals(7.9, episode.imdbRating)
    }

    @Test
    fun `uses episode average when show imdb rating is missing`() {
        given(tmdbClient.getTvShow(1396)).willReturn(
            TmdbTvShowDetail(
                id = 1396,
                name = "Breaking Bad",
                seasons = listOf(TmdbSeasonRef(seasonNumber = 1, episodeCount = 2)),
            ),
        )
        given(tmdbClient.getTvExternalIds(1396)).willReturn(TmdbExternalIds(imdbId = "tt0903747"))
        given(omdbClient.getTitle("tt0903747")).willReturn(OmdbTitleDetail(response = "True"))
        given(tmdbClient.getTvSeason(1396, 1)).willReturn(
            TmdbSeasonDetail(
                seasonNumber = 1,
                episodes = listOf(
                    TmdbEpisode(episodeNumber = 1, name = "Pilot"),
                    TmdbEpisode(episodeNumber = 2, name = "Cat's in the Bag"),
                ),
            ),
        )
        given(omdbClient.getSeason("tt0903747", 1)).willReturn(
            OmdbSeasonResponse(
                response = "True",
                episodes = listOf(
                    OmdbEpisodeSummary(episode = "1", imdbRating = "8.0"),
                    OmdbEpisodeSummary(episode = "2", imdbRating = "8.4"),
                ),
            ),
        )

        val detail = seriesDetailService.getDetail(1396)

        assertEquals(8.2, detail.imdbRating)
    }

    @Test
    fun `shows no imdb when omdb is unavailable`() {
        given(tmdbClient.getTvShow(1396)).willReturn(
            TmdbTvShowDetail(
                id = 1396,
                name = "Breaking Bad",
                seasons = listOf(TmdbSeasonRef(seasonNumber = 1, episodeCount = 1)),
            ),
        )
        given(tmdbClient.getTvExternalIds(1396)).willReturn(TmdbExternalIds(imdbId = null))
        given(tmdbClient.getTvSeason(1396, 1)).willReturn(
            TmdbSeasonDetail(
                seasonNumber = 1,
                episodes = listOf(TmdbEpisode(episodeNumber = 1, name = "Pilot", voteAverage = 8.5)),
            ),
        )

        val detail = seriesDetailService.getDetail(1396)
        val episode = detail.seasons.single().episodes.single()

        assertEquals(null, episode.imdbRating)
        assertEquals(null, detail.imdbRating)
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
