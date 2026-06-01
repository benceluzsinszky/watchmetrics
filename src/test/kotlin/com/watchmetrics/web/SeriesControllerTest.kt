package com.watchmetrics.web

import com.watchmetrics.model.EpisodeView
import com.watchmetrics.model.SeasonView
import com.watchmetrics.model.SeriesDetailView
import com.watchmetrics.service.SeriesDetailService
import com.watchmetrics.service.SeriesNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@WebMvcTest(SeriesController::class)
class SeriesControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var seriesDetailService: SeriesDetailService

    @Test
    fun `renders series detail page`() {
        given(seriesDetailService.getDetail(1396)).willReturn(
            SeriesDetailView(
                id = 1396,
                name = "Breaking Bad",
                overview = "A chemistry teacher.",
                posterUrl = "https://image.tmdb.org/t/p/w342/poster.jpg",
                firstAirYear = "2008",
                imdbRating = 9.5,
                rottenTomatoesScore = 96,
                metacriticScore = null,
                seasons = listOf(
                    SeasonView(
                        number = 1,
                        name = "Season 1",
                        episodes = listOf(
                            EpisodeView(1, "Pilot", null, 7.9, "2008-01-20"),
                        ),
                    ),
                ),
            ),
        )

        mockMvc.get("/series/1396").andExpect {
            status { isOk() }
            view { name("series/detail") }
            content { string(org.hamcrest.Matchers.containsString("Breaking Bad")) }
            content { string(org.hamcrest.Matchers.containsString("Pilot")) }
            content { string(org.hamcrest.Matchers.containsString("Episodes by season")) }
        }
    }

    @Test
    fun `returns not found page`() {
        given(seriesDetailService.getDetail(999)).willThrow(SeriesNotFoundException(999))

        mockMvc.get("/series/999").andExpect {
            status { isNotFound() }
            view { name("series/not-found") }
        }
    }
}
