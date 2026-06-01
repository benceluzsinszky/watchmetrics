package com.watchmetrics.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FinaleVerdictEvaluatorTest {

    @Test
    fun `returns ended well for strong finale`() {
        val result = FinaleVerdictEvaluator.evaluate(
            status = "Ended",
            highlights = highlights(
                lastEpisodeRating = 9.5,
                lastEpisodeRatingSource = RatingSource.IMDB,
                lastSeasonAverage = 9.0,
                lastSeasonAverageSource = RatingSource.IMDB,
            ),
        )

        assertNotNull(result)
        assertEquals(FinaleVerdict.ENDED_WELL, result!!.verdict)
        assertEquals(true, result.showInSearch)
    }

    @Test
    fun `returns weak ending for low finale`() {
        val result = FinaleVerdictEvaluator.evaluate(
            status = "Ended",
            highlights = highlights(
                lastEpisodeRating = 6.4,
                lastEpisodeRatingSource = RatingSource.IMDB,
                lastSeasonAverage = 6.4,
                lastSeasonAverageSource = RatingSource.IMDB,
            ),
        )

        assertNotNull(result)
        assertEquals(FinaleVerdict.ENDED_BADLY, result!!.verdict)
    }

    @Test
    fun `skips ongoing and mixed shows`() {
        assertNull(
            FinaleVerdictEvaluator.evaluate(
                status = "Returning Series",
                highlights = highlights(
                    lastEpisodeRating = 9.0,
                    lastEpisodeRatingSource = RatingSource.IMDB,
                    lastSeasonAverage = 9.0,
                    lastSeasonAverageSource = RatingSource.IMDB,
                ),
            ),
        )
        assertNull(
            FinaleVerdictEvaluator.evaluate(
                status = "Ended",
                highlights = highlights(
                    lastEpisodeRating = 7.5,
                    lastEpisodeRatingSource = RatingSource.IMDB,
                    lastSeasonAverage = 7.8,
                    lastSeasonAverageSource = RatingSource.IMDB,
                ),
            ),
        )
    }

    @Test
    fun `message uses season average when finale episode is unrated`() {
        val result = FinaleVerdictEvaluator.evaluate(
            status = "Ended",
            highlights = highlights(
                lastEpisodeRating = null,
                lastEpisodeRatingSource = null,
                lastSeasonAverage = 8.5,
                lastSeasonAverageSource = RatingSource.TMDB,
            ),
        )

        assertNotNull(result)
        assertEquals("Ended well · final season ★ 8.5 avg TMDB", result!!.message)
    }

    private fun highlights(
        lastEpisodeRating: Double?,
        lastEpisodeRatingSource: RatingSource?,
        lastSeasonAverage: Double?,
        lastSeasonAverageSource: RatingSource?,
    ): SeriesHighlightsView {
        val finale = SeriesFinaleHighlight(
            lastSeasonNumber = 8,
            lastSeasonName = "Season 8",
            lastSeasonAverage = lastSeasonAverage,
            lastSeasonAverageSource = lastSeasonAverageSource,
            lastEpisodeNumber = 6,
            lastEpisodeName = "Finale",
            lastEpisodeRating = lastEpisodeRating,
            lastEpisodeRatingSource = lastEpisodeRatingSource,
        )
        return SeriesHighlightsView(
            finale = finale,
            bestSeason = null,
            worstSeason = null,
            bestEpisode = null,
            worstEpisode = null,
        )
    }
}
