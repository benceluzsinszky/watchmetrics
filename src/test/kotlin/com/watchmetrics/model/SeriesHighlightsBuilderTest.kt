package com.watchmetrics.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SeriesHighlightsBuilderTest {

    @Test
    fun `builds finale best and worst highlights`() {
        val highlights = SeriesHighlightsBuilder.build(
            listOf(
                SeasonView(
                    number = 0,
                    name = "Specials",
                    episodes = listOf(EpisodeView(1, "Special", null, 10.0, null)),
                ),
                SeasonView(
                    number = 1,
                    name = "Season 1",
                    episodes = listOf(
                        EpisodeView(1, "Pilot", null, 8.0, null),
                        EpisodeView(2, "Low Point", null, 6.0, null),
                    ),
                ),
                SeasonView(
                    number = 2,
                    name = "Season 2",
                    episodes = listOf(
                        EpisodeView(1, "Opener", null, 9.0, null),
                        EpisodeView(2, "Finale", null, 7.0, null),
                    ),
                ),
            ),
        )

        assertNotNull(highlights.finale)
        assertEquals(2, highlights.finale!!.lastSeasonNumber)
        assertEquals(8.0, highlights.finale!!.lastSeasonAverage)
        assertEquals(2, highlights.finale!!.lastEpisodeNumber)
        assertEquals("Finale", highlights.finale!!.lastEpisodeName)
        assertEquals(7.0, highlights.finale!!.lastEpisodeRating)

        assertEquals(2, highlights.bestSeason!!.seasonNumber)
        assertEquals(8.0, highlights.bestSeason!!.averageRating)
        assertEquals(1, highlights.worstSeason!!.seasonNumber)
        assertEquals(7.0, highlights.worstSeason!!.averageRating)

        assertEquals("S2E1", highlights.bestEpisode!!.code)
        assertEquals(9.0, highlights.bestEpisode!!.imdbRating)
        assertEquals("S1E2", highlights.worstEpisode!!.code)
        assertEquals(6.0, highlights.worstEpisode!!.imdbRating)
    }
}
