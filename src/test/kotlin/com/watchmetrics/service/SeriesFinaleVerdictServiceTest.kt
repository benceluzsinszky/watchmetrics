package com.watchmetrics.service

import com.watchmetrics.client.OmdbClient
import com.watchmetrics.client.TmdbClient
import com.watchmetrics.model.FinaleVerdict
import com.watchmetrics.model.OmdbEpisodeSummary
import com.watchmetrics.model.OmdbSeasonResponse
import com.watchmetrics.model.TmdbEpisode
import com.watchmetrics.model.TmdbExternalIds
import com.watchmetrics.model.TmdbSeasonDetail
import com.watchmetrics.model.TmdbSeasonRef
import com.watchmetrics.model.TmdbTvShowDetail
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class SeriesFinaleVerdictServiceTest {

    @Mock
    private lateinit var tmdbClient: TmdbClient

    @Mock
    private lateinit var omdbClient: OmdbClient

    @InjectMocks
    private lateinit var seriesFinaleVerdictService: SeriesFinaleVerdictService

    @Test
    fun `resolves verdict from last season only`() {
        given(tmdbClient.getTvShow(1399)).willReturn(
            TmdbTvShowDetail(
                id = 1399,
                name = "Game of Thrones",
                status = "Ended",
                seasons = listOf(
                    TmdbSeasonRef(seasonNumber = 1, episodeCount = 1),
                    TmdbSeasonRef(seasonNumber = 8, episodeCount = 1),
                ),
            ),
        )
        given(tmdbClient.getTvExternalIds(1399)).willReturn(TmdbExternalIds(imdbId = "tt0944947"))
        given(tmdbClient.getTvSeason(1399, 8)).willReturn(
            TmdbSeasonDetail(
                seasonNumber = 8,
                name = "Season 8",
                episodes = listOf(
                    TmdbEpisode(episodeNumber = 6, name = "The Iron Throne", voteAverage = 6.4),
                ),
            ),
        )
        given(omdbClient.getSeason("tt0944947", 8)).willReturn(
            OmdbSeasonResponse(
                response = "True",
                episodes = listOf(OmdbEpisodeSummary(episode = "6", imdbRating = "6.4")),
            ),
        )

        val verdict = seriesFinaleVerdictService.resolve(1399)

        assertNotNull(verdict)
        assertEquals(FinaleVerdict.ENDED_BADLY, verdict!!.verdict)
    }

    @Test
    fun `skips ongoing shows without loading seasons`() {
        given(tmdbClient.getTvShow(1)).willReturn(
            TmdbTvShowDetail(
                id = 1,
                name = "Succession",
                status = "Returning Series",
                seasons = listOf(TmdbSeasonRef(seasonNumber = 1, episodeCount = 10)),
            ),
        )

        assertNull(seriesFinaleVerdictService.resolve(1))
    }
}
