package com.watchmetrics.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RatingGridBuilderTest {

    @Test
    fun `uses highest episode number not list size for column count`() {
        val grid = RatingGridBuilder.build(
            listOf(
                SeasonView(
                    number = 1,
                    name = "Season 1",
                    episodes = listOf(
                        EpisodeView(1, "Pilot", null, 8.0, null, null),
                        EpisodeView(2, "Cat's in the Bag", null, 7.5, null, null),
                        EpisodeView(5, "Gray Matter", null, 8.2, null, null),
                    ),
                ),
            ),
        )

        assertEquals(5, grid.maxEpisodes)
        assertEquals(5, grid.rows.single().cells.size)
        assertEquals(null, grid.rows.single().cells[2].episodeNumber)
        assertEquals(8.2, grid.rows.single().cells[4].displayRating)
    }

    @Test
    fun `builds grid aligned to max episode count across seasons`() {
        val grid = RatingGridBuilder.build(
            listOf(
                SeasonView(
                    number = 1,
                    name = "Season 1",
                    episodes = listOf(
                        EpisodeView(1, "Pilot", null, 8.0, null, null),
                        EpisodeView(2, "Cat's in the Bag", null, 7.5, null, null),
                    ),
                ),
                SeasonView(
                    number = 2,
                    name = "Season 2",
                    episodes = listOf(
                        EpisodeView(1, "Seven Thirty-Seven", null, 8.8, null, null),
                    ),
                ),
            ),
        )

        assertEquals(2, grid.maxEpisodes)
        assertEquals(2, grid.rows.size)
        assertEquals(7.5, grid.rows[0].cells[1].displayRating)
        assertEquals(false, grid.rows[1].cells[1].hasEpisode)
        assertEquals(8.0, grid.rows[0].cells[0].displayRating)
    }

    @Test
    fun `excludes specials from heatmap`() {
        val grid = RatingGridBuilder.build(
            listOf(
                SeasonView(
                    number = 0,
                    name = "Specials",
                    episodes = listOf(EpisodeView(1, "Minisode", null, 9.0, null, null)),
                ),
                SeasonView(
                    number = 1,
                    name = "Season 1",
                    episodes = listOf(EpisodeView(1, "Pilot", null, 8.0, null, null)),
                ),
            ),
        )

        assertEquals(1, grid.rows.size)
        assertEquals("S1", grid.rows.single().seasonLabel)
    }
}
