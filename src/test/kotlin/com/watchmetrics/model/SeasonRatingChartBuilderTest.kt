package com.watchmetrics.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeasonRatingChartBuilderTest {

    @Test
    fun `computes average imdb per season excluding specials`() {
        val chart = SeasonRatingChartBuilder.build(
            listOf(
                SeasonView(
                    number = 0,
                    name = "Specials",
                    episodes = listOf(EpisodeView(1, "Special", null, 9.5, null)),
                ),
                SeasonView(
                    number = 1,
                    name = "Season 1",
                    episodes = listOf(
                        EpisodeView(1, "Pilot", null, 8.0, null),
                        EpisodeView(2, "Cat's in the Bag", null, 8.4, null),
                    ),
                ),
                SeasonView(
                    number = 2,
                    name = "Season 2",
                    episodes = listOf(
                        EpisodeView(1, "Seven Thirty-Seven", null, 8.8, null),
                        EpisodeView(2, "Grilled", null, null, null),
                    ),
                ),
            ),
        )

        assertEquals(2, chart.bars.size)
        assertEquals(8.2, chart.bars[0].averageRating)
        assertEquals(8.8, chart.bars[1].averageRating)
        assertTrue(chart.hasData)
        assertNotNull(chart.linePolylinePoints)
    }

    @Test
    fun `returns no line when fewer than two rated seasons`() {
        val chart = SeasonRatingChartBuilder.build(
            listOf(
                SeasonView(
                    number = 1,
                    name = "Season 1",
                    episodes = listOf(EpisodeView(1, "Pilot", null, 8.0, null)),
                ),
            ),
        )

        assertNull(chart.linePolylinePoints)
        assertEquals(80.0, chart.bars.single().barHeightPercent)
        assertEquals(160, chart.bars.single().barHeightPx)
    }
}
