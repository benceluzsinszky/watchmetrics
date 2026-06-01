package com.watchmetrics.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class SeriesController {

    @GetMapping("/series/{tmdbId}")
    fun show(@PathVariable tmdbId: Int, model: Model): String {
        model.addAttribute("tmdbId", tmdbId)
        return "series/detail"
    }
}
