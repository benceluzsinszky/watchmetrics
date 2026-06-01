package com.watchmetrics.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OMDB_API_KEY", matches = ".+")
class OmdbPropertiesBindingTest {

    @Autowired
    private lateinit var omdbProperties: OmdbProperties

    @Test
    fun `binds omdb api key from environment`() {
        assertTrue(omdbProperties.isConfigured)
    }
}
