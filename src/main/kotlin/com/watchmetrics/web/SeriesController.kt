package com.watchmetrics.web

import com.watchmetrics.service.SeriesDetailException
import com.watchmetrics.service.SeriesDetailService
import com.watchmetrics.service.SeriesNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus

@Controller
class SeriesController(
    private val seriesDetailService: SeriesDetailService,
) {

    @GetMapping("/series/{tmdbId}")
    fun show(@PathVariable tmdbId: Int, model: Model): String {
        val series = seriesDetailService.getDetail(tmdbId)
        model.addAttribute("series", series)
        return "series/detail"
    }

    @ExceptionHandler(SeriesNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: SeriesNotFoundException, model: Model): String {
        model.addAttribute("tmdbId", ex.tmdbId)
        return "series/not-found"
    }

    @ExceptionHandler(SeriesDetailException::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleDetailError(ex: SeriesDetailException, model: Model): String {
        model.addAttribute("message", ex.message)
        return "series/error"
    }
}
