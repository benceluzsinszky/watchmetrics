package com.watchmetrics.web

import com.watchmetrics.model.FinaleVerdictResult
import com.watchmetrics.model.SeriesSearchPageView
import com.watchmetrics.model.SeriesSearchResultView
import com.watchmetrics.service.SeriesSearchService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@WebMvcTest(HomeController::class)
class HomeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var seriesSearchService: SeriesSearchService

    @Test
    fun `htmx request returns search fragment`() {
        given(seriesSearchService.search("breaking bad")).willReturn(
            SeriesSearchPageView(
                results = listOf(
                    SeriesSearchResultView(
                        id = 1396,
                        name = "Breaking Bad",
                        overview = null,
                        posterUrl = null,
                        firstAirYear = "2008",
                        finaleVerdict = FinaleVerdictResult(
                            verdict = com.watchmetrics.model.FinaleVerdict.ENDED_WELL,
                            message = "Ended well · finale ★ 9.5",
                        ),
                    ),
                ),
                totalResults = 1,
            ),
        )

        mockMvc.get("/") {
            header("HX-Request", "true")
            param("q", "breaking bad")
        }.andExpect {
            status { isOk() }
            view { name("fragments/search-results :: results") }
            content { string(org.hamcrest.Matchers.containsString("Breaking Bad")) }
            content { string(org.hamcrest.Matchers.containsString("Ended well")) }
        }
    }

    @Test
    fun `full page load with query renders index and results`() {
        given(seriesSearchService.search("succession")).willReturn(
            SeriesSearchPageView(
                results = listOf(
                    SeriesSearchResultView(
                        id = 1,
                        name = "Succession",
                        overview = null,
                        posterUrl = null,
                        firstAirYear = null,
                        finaleVerdict = null,
                    ),
                ),
                totalResults = 1,
            ),
        )

        mockMvc.get("/") {
            param("q", "succession")
        }.andExpect {
            status { isOk() }
            view { name("index") }
            content { string(org.hamcrest.Matchers.containsString("Succession")) }
            content { string(org.hamcrest.Matchers.containsString("value=\"succession\"")) }
        }
    }

    @Test
    fun `search path redirects to query param on home`() {
        mockMvc.get("/search") {
            param("q", "breaking bad")
        }.andExpect {
            status { isFound() }
            header { string("Location", "/?q=breaking%20bad") }
        }
    }
}
