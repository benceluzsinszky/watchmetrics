package com.watchmetrics.client

import com.watchmetrics.config.OmdbConfig
import com.watchmetrics.config.OmdbProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OMDB_API_KEY", matches = ".+")
class OmdbClientIntegrationTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("omdb.api-key") { System.getenv("OMDB_API_KEY") }
        }
    }

    @Autowired
    private lateinit var omdbClient: OmdbClient

    @Test
    fun `loads game of thrones season ratings from live omdb`() {
        val season = omdbClient.getSeason("tt0944947", 1)

        assertNotNull(season)
        assertTrue(season!!.episodes.any { it.imdbRating == "8.9" })
    }
}
