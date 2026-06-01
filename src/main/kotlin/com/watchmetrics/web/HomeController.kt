package com.watchmetrics.web

import com.watchmetrics.service.SeriesSearchException
import com.watchmetrics.service.SeriesSearchService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets

@Controller
class HomeController(
    private val seriesSearchService: SeriesSearchService,
) {

    @GetMapping("/")
    fun home(
        @RequestParam(name = "q", required = false) query: String?,
        @RequestHeader(value = "HX-Request", required = false) hxRequest: String?,
        model: Model,
    ): String {
        model.addAttribute("title", "Watchmetrics")
        populateSearch(model, query)
        return if (hxRequest != null) SEARCH_FRAGMENT else "index"
    }

    @GetMapping("/search")
    fun searchRedirect(@RequestParam(name = "q", required = false) query: String?): String {
        val trimmed = query?.trim().orEmpty()
        if (trimmed.isBlank()) {
            return "redirect:/"
        }
        val encoded = UriUtils.encodeQueryParam(trimmed, StandardCharsets.UTF_8)
        return "redirect:/?q=$encoded"
    }

    @ExceptionHandler(SeriesSearchException::class)
    fun handleSearchError(
        ex: SeriesSearchException,
        @RequestParam(name = "q", required = false) query: String?,
        @RequestHeader(value = "HX-Request", required = false) hxRequest: String?,
        model: Model,
    ): String {
        model.addAttribute("title", "Watchmetrics")
        model.addAttribute("query", query?.trim().orEmpty())
        model.addAttribute("results", emptyList<Any>())
        model.addAttribute("totalResults", 0)
        model.addAttribute("error", ex.message)
        return if (hxRequest != null) SEARCH_FRAGMENT else "index"
    }

    private fun populateSearch(model: Model, query: String?) {
        val trimmed = query?.trim().orEmpty()
        if (trimmed.isBlank()) {
            model.addAttribute("query", "")
            model.addAttribute("results", emptyList<Any>())
            model.addAttribute("totalResults", 0)
            model.addAttribute("error", null as String?)
            return
        }

        val response = seriesSearchService.search(trimmed)
        model.addAttribute("query", trimmed)
        model.addAttribute("results", response.results)
        model.addAttribute("totalResults", response.totalResults)
        model.addAttribute("error", null as String?)
    }

    companion object {
        private const val SEARCH_FRAGMENT = "fragments/search-results :: results"
    }
}
