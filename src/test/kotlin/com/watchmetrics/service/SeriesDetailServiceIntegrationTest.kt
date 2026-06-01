package com.watchmetrics.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OMDB_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TMDB_ACCESS_TOKEN", matches = ".+")
class SeriesDetailServiceIntegrationTest {

    @Autowired
    private lateinit var seriesDetailService: SeriesDetailService

    @Test
    fun `loads imdb ratings for game of thrones`() {
        val detail = seriesDetailService.getDetail(1399)

        val pilot = detail.seasons.first { it.number == 1 }.episodes.first { it.number == 1 }
        assertNotNull(pilot.imdbRating, "Expected IMDb rating for S01E01")
    }
}
