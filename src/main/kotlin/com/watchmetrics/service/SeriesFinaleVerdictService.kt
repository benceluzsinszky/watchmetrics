package com.watchmetrics.service

import com.watchmetrics.client.OmdbClient
import com.watchmetrics.client.TmdbClient
import com.watchmetrics.config.CacheNames
import com.watchmetrics.model.EpisodeView
import com.watchmetrics.model.FinaleVerdictEvaluator
import com.watchmetrics.model.FinaleVerdictResult
import com.watchmetrics.model.OmdbParsing
import com.watchmetrics.model.SeasonView
import com.watchmetrics.model.SeriesHighlightsBuilder
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException

@Service
class SeriesFinaleVerdictService(
    private val tmdbClient: TmdbClient,
    private val omdbClient: OmdbClient,
) {

    @Cacheable(
        cacheNames = [CacheNames.FINALE_VERDICT],
        key = "#tmdbId",
        unless = "#result == null",
    )
    fun resolve(tmdbId: Int): FinaleVerdictResult? {
        return try {
            val show = tmdbClient.getTvShow(tmdbId)
            if (show.status != null && show.status !in ENDED_STATUSES) {
                return null
            }

            val lastSeasonRef = show.seasons
                .filter { it.seasonNumber != 0 && it.episodeCount > 0 }
                .maxByOrNull { it.seasonNumber }
                ?: return null

            val imdbId = runCatching { tmdbClient.getTvExternalIds(tmdbId).imdbId }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }

            val season = tmdbClient.getTvSeason(tmdbId, lastSeasonRef.seasonNumber)
            val omdbRatingsByEpisode = omdbRatingsByEpisode(imdbId, season.seasonNumber)

            val episodes = season.episodes
                .sortedBy { it.episodeNumber }
                .map { episode ->
                    EpisodeView(
                        number = episode.episodeNumber,
                        name = episode.name,
                        overview = null,
                        imdbRating = omdbRatingsByEpisode[episode.episodeNumber],
                        tmdbRating = episode.voteAverage?.takeIf { it > 0 },
                        airDate = episode.airDate,
                    )
                }

            val seasonView = SeasonView(
                number = season.seasonNumber,
                name = season.name?.takeIf { it.isNotBlank() }
                    ?: lastSeasonRef.name?.takeIf { it.isNotBlank() }
                    ?: defaultSeasonName(season.seasonNumber),
                episodes = episodes,
            )

            val highlights = SeriesHighlightsBuilder.build(listOf(seasonView))
            FinaleVerdictEvaluator.evaluate(show.status, highlights)
        } catch (_: RestClientResponseException) {
            null
        }
    }

    private fun omdbRatingsByEpisode(imdbId: String?, seasonNumber: Int): Map<Int, Double> =
        imdbId
            ?.let { omdbClient.getSeason(it, seasonNumber) }
            ?.episodes
            ?.mapNotNull { episode ->
                val number = OmdbParsing.parseEpisodeNumber(episode.episode) ?: return@mapNotNull null
                val rating = OmdbParsing.parseRating(episode.imdbRating) ?: return@mapNotNull null
                number to rating
            }
            ?.toMap()
            ?: emptyMap()

    private fun defaultSeasonName(seasonNumber: Int): String =
        if (seasonNumber == 0) "Specials" else "Season $seasonNumber"

    companion object {
        private val ENDED_STATUSES = setOf("Ended", "Canceled")
    }
}
