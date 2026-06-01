package com.watchmetrics.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ApiConfigStartupCheck(
    private val tmdbProperties: TmdbProperties,
    private val omdbProperties: OmdbProperties,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (!tmdbProperties.isConfigured) {
            log.warn("TMDB is not configured — search will fail. Add TMDB_ACCESS_TOKEN or TMDB_API_KEY to .env and restart.")
        }
        if (!omdbProperties.isConfigured) {
            log.warn("OMDb is not configured — IMDb ratings will show as unavailable. Add OMDB_API_KEY to .env and restart the app (DevTools reload is not enough).")
        }
    }
}
