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
                lastSeasonAverage = 9.0,
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
                lastSeasonAverage = 6.4,
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
                highlights = highlights(lastEpisodeRating = 9.0, lastSeasonAverage = 9.0),
            ),
        )
        assertNull(
            FinaleVerdictEvaluator.evaluate(
                status = "Ended",
                highlights = highlights(lastEpisodeRating = 7.5, lastSeasonAverage = 7.8),
            ),
        )
    }

    @Test
    fun `message uses season average when finale episode is unrated`() {
        val result = FinaleVerdictEvaluator.evaluate(
            status = "Ended",
            highlights = highlights(
                lastEpisodeRating = null,
                lastSeasonAverage = 8.5,
            ),
        )

        assertNotNull(result)
        assertEquals("Ended well · final season ★ 8.5 avg", result!!.message)
    }

    private fun highlights(
        lastEpisodeRating: Double?,
        lastSeasonAverage: Double?,
    ): SeriesHighlightsView {
        val finale = SeriesFinaleHighlight(
            lastSeasonNumber = 8,
            lastSeasonName = "Season 8",
            lastSeasonAverage = lastSeasonAverage,
            lastEpisodeNumber = 6,
            lastEpisodeName = "Finale",
            lastEpisodeRating = lastEpisodeRating,
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
