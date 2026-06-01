package com.watchmetrics.client

import com.watchmetrics.config.CacheConfig
import com.watchmetrics.config.TmdbConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import kotlin.test.assertEquals

@RestClientTest(TmdbClient::class)
@Import(TmdbConfig::class, CacheConfig::class)
@TestPropertySource(
    properties = [
        "tmdb.access-token=test-token",
        "tmdb.api-key=test-key",
    ],
)
class TmdbClientCacheTest {

    @Autowired
    private lateinit var tmdbClient: TmdbClient

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Test
    fun `getTvShow uses cache on repeat lookup`() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/tv/1396")))
            .andRespond(
                withSuccess(
                    """
                    {
                      "id": 1396,
                      "name": "Breaking Bad",
                      "seasons": []
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        val first = tmdbClient.getTvShow(1396)
        val second = tmdbClient.getTvShow(1396)

        assertEquals(first.name, second.name)
        server.verify()
    }

    @Test
    fun `searchTv uses cache on repeat lookup`() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/search/tv")))
            .andRespond(
                withSuccess(
                    """
                    {
                      "page": 1,
                      "results": [{"id": 1396, "name": "Breaking Bad"}],
                      "total_pages": 1,
                      "total_results": 1
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        val first = tmdbClient.searchTv("Breaking Bad")
        val second = tmdbClient.searchTv("breaking bad")

        assertEquals(first.results.size, second.results.size)
        server.verify()
    }
}
