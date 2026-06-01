package com.watchmetrics.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("title", "Watchmetrics")
        model.addAttribute("query", "")
        model.addAttribute("results", emptyList<Any>())
        model.addAttribute("totalResults", 0)
        model.addAttribute("error", null as String?)
        return "index"
    }
}
