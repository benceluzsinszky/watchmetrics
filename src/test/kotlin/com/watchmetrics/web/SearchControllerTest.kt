package com.watchmetrics.web

import com.watchmetrics.model.TmdbTvSearchResponse
import com.watchmetrics.model.TmdbTvShowSummary
import com.watchmetrics.service.SeriesSearchService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SearchController::class)
class SearchControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var seriesSearchService: SeriesSearchService

    @Test
    fun `search with blank query returns empty results`() {
        mockMvc.get("/search")
            .andExpect {
                status { isOk() }
                content { string(org.hamcrest.Matchers.containsString("search-results")) }
            }
    }

    @Test
    fun `search returns matching shows`() {
        given(seriesSearchService.search("breaking bad")).willReturn(
            TmdbTvSearchResponse(
                results = listOf(
                    TmdbTvShowSummary(
                        id = 1396,
                        name = "Breaking Bad",
                        firstAirDate = "2008-01-20",
                        overview = "A chemistry teacher turned meth maker.",
                    ),
                ),
                totalResults = 1,
            ),
        )

        mockMvc.get("/search") {
            param("q", "breaking bad")
        }.andExpect {
            status { isOk() }
            content { string(org.hamcrest.Matchers.containsString("Breaking Bad")) }
            content { string(org.hamcrest.Matchers.containsString("/series/1396")) }
        }
    }
}
