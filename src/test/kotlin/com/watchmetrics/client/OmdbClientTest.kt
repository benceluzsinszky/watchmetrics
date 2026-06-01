package com.watchmetrics.client

import com.watchmetrics.config.OmdbConfig
import com.watchmetrics.config.OmdbProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import kotlin.test.assertEquals

@RestClientTest(OmdbClient::class)
@Import(OmdbConfig::class)
@TestPropertySource(properties = ["omdb.api-key=test-omdb-key"])
class OmdbClientTest {

    @Autowired
    private lateinit var omdbClient: OmdbClient

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Test
    fun `getSeason returns episode ratings`() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("omdbapi.com")))
            .andExpect(queryParam("apikey", "test-omdb-key"))
            .andExpect(queryParam("i", "tt0903747"))
            .andExpect(queryParam("Season", "1"))
            .andRespond(
                withSuccess(
                    """
                    {
                      "Season": "1",
                      "Episodes": [
                        {
                          "Title": "Pilot",
                          "Episode": "1",
                          "imdbRating": "7.9"
                        }
                      ],
                      "Response": "True"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        val response = omdbClient.getSeason("tt0903747", 1)

        assertEquals(1, response?.episodes?.size)
        assertEquals("7.9", response?.episodes?.single()?.imdbRating)
        server.verify()
    }
}
