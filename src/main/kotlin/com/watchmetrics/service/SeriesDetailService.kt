package com.watchmetrics.service

import com.watchmetrics.client.TmdbClient
import com.watchmetrics.model.EpisodeView
import com.watchmetrics.model.SeasonView
import com.watchmetrics.model.SeriesDetailView
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

@Service
class SeriesDetailService(
    private val tmdbClient: TmdbClient,
) {

    fun getDetail(tmdbId: Int): SeriesDetailView {
        return try {
            val show = tmdbClient.getTvShow(tmdbId)
            val seasons = show.seasons
                .filter { it.episodeCount > 0 }
                .sortedBy { it.seasonNumber }
                .map { seasonRef ->
                    val season = tmdbClient.getTvSeason(tmdbId, seasonRef.seasonNumber)
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
                                    rating = episode.voteAverage,
                                    airDate = episode.airDate,
                                )
                            },
                    )
                }

            SeriesDetailView(
                id = show.id,
                name = show.name,
                overview = show.overview,
                posterUrl = show.posterUrl,
                firstAirYear = show.firstAirDate?.take(4),
                voteAverage = show.voteAverage?.takeIf { it > 0 },
                seasons = seasons,
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
}

class SeriesNotFoundException(val tmdbId: Int) : RuntimeException("Series not found.")

class SeriesDetailException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
