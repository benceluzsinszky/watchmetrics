package com.watchmetrics.service

import com.watchmetrics.client.OmdbClient
import com.watchmetrics.client.TmdbClient
import com.watchmetrics.model.EpisodeView
import com.watchmetrics.model.OmdbParsing
import com.watchmetrics.model.RatingGridBuilder
import com.watchmetrics.model.SeasonRatingChartBuilder
import com.watchmetrics.model.SeasonView
import com.watchmetrics.model.SeriesDetailView
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

@Service
class SeriesDetailService(
    private val tmdbClient: TmdbClient,
    private val omdbClient: OmdbClient,
) {

    fun getDetail(tmdbId: Int): SeriesDetailView {
        return try {
            val show = tmdbClient.getTvShow(tmdbId)
            val imdbId = runCatching { tmdbClient.getTvExternalIds(tmdbId).imdbId }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }

            val omdbShow = imdbId?.let { omdbClient.getTitle(it) }

            val seasons = show.seasons
                .filter { it.episodeCount > 0 }
                .sortedBy { seasonSortKey(it.seasonNumber) }
                .map { seasonRef ->
                    val season = tmdbClient.getTvSeason(tmdbId, seasonRef.seasonNumber)
                    val omdbRatingsByEpisode = imdbId
                        ?.let { omdbClient.getSeason(it, season.seasonNumber) }
                        ?.episodes
                        ?.mapNotNull { episode ->
                            val number = OmdbParsing.parseEpisodeNumber(episode.episode) ?: return@mapNotNull null
                            val rating = OmdbParsing.parseRating(episode.imdbRating) ?: return@mapNotNull null
                            number to rating
                        }
                        ?.toMap()
                        ?: emptyMap()

                    SeasonView(
                        number = season.seasonNumber,
                        name = season.name?.takeIf { it.isNotBlank() }
                            ?: seasonRef.name?.takeIf { it.isNotBlank() }
                            ?: defaultSeasonName(season.seasonNumber),
                        episodes = season.episodes
                            .sortedBy { it.episodeNumber }
                            .map { episode ->
                                EpisodeView(
                                    number = episode.episodeNumber,
                                    name = episode.name,
                                    overview = episode.overview,
                                    imdbRating = omdbRatingsByEpisode[episode.episodeNumber],
                                    airDate = episode.airDate,
                                )
                            },
                    )
                }

            val imdbRating = resolveSeriesImdbRating(omdbShow?.imdbRating, seasons)

            SeriesDetailView(
                id = show.id,
                name = show.name,
                overview = show.overview,
                posterUrl = show.posterUrl,
                firstAirYear = show.firstAirDate?.take(4),
                imdbRating = imdbRating,
                rottenTomatoesScore = omdbShow?.rottenTomatoesScore,
                metacriticScore = omdbShow?.metacriticScore,
                seasons = seasons,
                ratingGrid = RatingGridBuilder.build(seasons),
                seasonRatingChart = SeasonRatingChartBuilder.build(seasons),
            )
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.value() == 404) {
                throw SeriesNotFoundException(tmdbId)
            }
            throw SeriesDetailException("Could not load series details right now.", ex)
        } catch (ex: RestClientException) {
            throw SeriesDetailException("Could not load series details right now.", ex)
        }
    }

    private fun defaultSeasonName(seasonNumber: Int): String =
        if (seasonNumber == 0) "Specials" else "Season $seasonNumber"

    private fun seasonSortKey(seasonNumber: Int): Int =
        if (seasonNumber == 0) Int.MAX_VALUE else seasonNumber

    private fun resolveSeriesImdbRating(omdbShowRating: String?, seasons: List<SeasonView>): Double? {
        OmdbParsing.parseRating(omdbShowRating)?.let { return it }

        val episodeRatings = seasons.flatMap { season ->
            season.episodes.mapNotNull { it.imdbRating }
        }
        if (episodeRatings.isEmpty()) {
            return null
        }
        return episodeRatings.average()
    }
}

class SeriesNotFoundException(val tmdbId: Int) : RuntimeException("Series not found.")

class SeriesDetailException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
