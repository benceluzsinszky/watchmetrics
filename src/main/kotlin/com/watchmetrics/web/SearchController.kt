package com.watchmetrics.web

import com.watchmetrics.service.SeriesSearchException
import com.watchmetrics.service.SeriesSearchService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SearchController(
    private val seriesSearchService: SeriesSearchService,
) {

    @GetMapping("/search")
    fun search(@RequestParam(name = "q", required = false) query: String?, model: Model): String {
        val trimmed = query?.trim().orEmpty()
        if (trimmed.isBlank()) {
            populateEmpty(model)
            return FRAGMENT
        }

        val response = seriesSearchService.search(trimmed)
        model.addAttribute("query", trimmed)
        model.addAttribute("results", response.results)
        model.addAttribute("totalResults", response.totalResults)
        model.addAttribute("error", null as String?)
        return FRAGMENT
    }

    @ExceptionHandler(SeriesSearchException::class)
    fun handleSearchError(
        ex: SeriesSearchException,
        @RequestParam(name = "q", required = false) query: String?,
        model: Model,
    ): String {
        model.addAttribute("query", query?.trim().orEmpty())
        model.addAttribute("results", emptyList<Any>())
        model.addAttribute("totalResults", 0)
        model.addAttribute("error", ex.message)
        return FRAGMENT
    }

    private fun populateEmpty(model: Model) {
        model.addAttribute("query", "")
        model.addAttribute("results", emptyList<Any>())
        model.addAttribute("totalResults", 0)
        model.addAttribute("error", null as String?)
    }

    companion object {
        private const val FRAGMENT = "fragments/search-results :: results"
    }
}
