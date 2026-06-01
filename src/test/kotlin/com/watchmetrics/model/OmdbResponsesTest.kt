package com.watchmetrics.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OmdbResponsesTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun `deserializes omdb season response`() {
        val json = """
            {
              "Title": "Game of Thrones",
              "Season": "1",
              "Episodes": [
                {
                  "Title": "Winter Is Coming",
                  "Episode": "1",
                  "imdbRating": "8.9"
                }
              ],
              "Response": "True"
            }
        """.trimIndent()

        val response = mapper.readValue<OmdbSeasonResponse>(json)

        assertEquals("True", response.response)
        assertEquals(1, response.episodes.size)
        assertEquals("1", response.episodes.single().episode)
        assertEquals("8.9", response.episodes.single().imdbRating)
    }
}
