package com.watchmetrics.client

import com.watchmetrics.config.TmdbConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import kotlin.test.assertEquals

@RestClientTest(TmdbClient::class)
@Import(TmdbConfig::class)
@TestPropertySource(
    properties = [
        "tmdb.access-token=test-token",
        "tmdb.api-key=test-key",
    ],
)
class TmdbClientTest {

    @Autowired
    private lateinit var tmdbClient: TmdbClient

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Test
    fun `searchTv returns parsed results`() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/search/tv")))
            .andExpect(header("Authorization", "Bearer test-token"))
            .andRespond(
                withSuccess(
                    """
                    {
                      "page": 1,
                      "results": [
                        {
                          "id": 1396,
                          "name": "Breaking Bad",
                          "poster_path": "/poster.jpg",
                          "first_air_date": "2008-01-20"
                        }
                      ],
                      "total_pages": 1,
                      "total_results": 1
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        val response = tmdbClient.searchTv("breaking bad")

        assertEquals(1, response.results.size)
        assertEquals("Breaking Bad", response.results.first().name)
        assertEquals(1396, response.results.first().id)
        server.verify()
    }
}
