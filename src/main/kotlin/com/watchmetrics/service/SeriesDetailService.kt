package com.watchmetrics.service

import com.watchmetrics.client.OmdbClient
import com.watchmetrics.client.TmdbClient
import com.watchmetrics.config.CacheNames
import com.watchmetrics.model.EpisodeView
import com.watchmetrics.model.OmdbParsing
import com.watchmetrics.model.RatingGridBuilder
import com.watchmetrics.model.RatingResolver
import com.watchmetrics.model.RatingSource
import com.watchmetrics.model.SeasonRatingChartBuilder
import com.watchmetrics.model.SeriesHighlightsBuilder
import com.watchmetrics.model.SeasonView
import com.watchmetrics.model.SeriesDetailView
import com.watchmetrics.model.OmdbSeasonResponse
import com.watchmetrics.model.TmdbEpisode
import com.watchmetrics.model.TmdbSeasonDetail
import com.watchmetrics.model.TmdbSeasonRef
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@Service
class SeriesDetailService(
    private val tmdbClient: TmdbClient,
    private val omdbClient: OmdbClient,
) {

    @Cacheable(cacheNames = [CacheNames.SERIES_DETAIL], key = "#tmdbId")
    fun getDetail(tmdbId: Int): SeriesDetailView {
        return try {
            val show = tmdbClient.getTvShow(tmdbId)
            val seasonRefs = show.seasons
                .filter { it.episodeCount > 0 }
                .sortedBy { seasonSortKey(it.seasonNumber) }

            Executors.newVirtualThreadPerTaskExecutor().use { executor ->
                val imdbIdFuture = executor.submit(
                    Callable {
                        runCatching { tmdbClient.getTvExternalIds(tmdbId).imdbId }
                            .getOrNull()
                            ?.takeIf { it.isNotBlank() }
                    },
                )
                val tmdbSeasonFutures = seasonRefs.map { seasonRef ->
                    executor.submit(
                        Callable {
                            SeasonLoadContext(
                                season = tmdbClient.getTvSeason(tmdbId, seasonRef.seasonNumber),
                                seasonRef = seasonRef,
                            )
                        },
                    )
                }
                val imdbId = imdbIdFuture.get()
                val omdbShowFuture = imdbId?.let { id ->
                    executor.submit(Callable { omdbClient.getTitle(id) })
                }
                val omdbSeasonFutures = seasonRefs.map { seasonRef ->
                    executor.submit(
                        Callable {
                            imdbId
                                ?.let { omdbClient.getSeason(it, seasonRef.seasonNumber) }
                                ?.let(::omdbRatingsByEpisode)
                                ?: emptyMap()
                        },
                    )
                }

                val seasons = tmdbSeasonFutures.mapIndexed { index, seasonFuture ->
                    val context = seasonFuture.get()
                    buildSeasonView(
                        seasonRef = context.seasonRef,
                        seasonNumber = context.season.seasonNumber,
                        seasonName = context.season.name,
                        episodes = context.season.episodes,
                        omdbRatingsByEpisode = omdbSeasonFutures[index].get(),
                    )
                }

                val omdbShow = omdbShowFuture?.get()
                val (displayRating, ratingSource) = resolveShowRating(
                    omdbShowRating = omdbShow?.imdbRating,
                    showVoteAverage = show.voteAverage,
                    seasons = seasons,
                )

                SeriesDetailView(
                    id = show.id,
                    name = show.name,
                    overview = show.overview,
                    posterUrl = show.posterUrl,
                    firstAirYear = show.firstAirDate?.take(4),
                    status = show.status,
                    displayRating = displayRating,
                    ratingSource = ratingSource,
                    rottenTomatoesScore = omdbShow?.rottenTomatoesScore,
                    metacriticScore = omdbShow?.metacriticScore,
                    seasons = seasons,
                    ratingGrid = RatingGridBuilder.build(seasons),
                    seasonRatingChart = SeasonRatingChartBuilder.build(seasons),
                    highlights = SeriesHighlightsBuilder.build(seasons),
                )
            }
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.value() == 404) {
                throw SeriesNotFoundException(tmdbId)
            }
            throw SeriesDetailException("Could not load series details right now.", ex)
        } catch (ex: RestClientException) {
            throw SeriesDetailException("Could not load series details right now.", ex)
        }
    }

    private data class SeasonLoadContext(
        val season: TmdbSeasonDetail,
        val seasonRef: TmdbSeasonRef,
    )

    private fun omdbRatingsByEpisode(response: OmdbSeasonResponse?): Map<Int, Double> =
        response
            ?.episodes
            ?.mapNotNull { episode ->
                val number = OmdbParsing.parseEpisodeNumber(episode.episode) ?: return@mapNotNull null
                val rating = OmdbParsing.parseRating(episode.imdbRating) ?: return@mapNotNull null
                number to rating
            }
            ?.toMap()
            ?: emptyMap()

    private fun buildSeasonView(
        seasonRef: TmdbSeasonRef,
        seasonNumber: Int,
        seasonName: String?,
        episodes: List<TmdbEpisode>,
        omdbRatingsByEpisode: Map<Int, Double>,
    ): SeasonView =
        SeasonView(
            number = seasonNumber,
            name = seasonName?.takeIf { it.isNotBlank() }
                ?: seasonRef.name?.takeIf { it.isNotBlank() }
                ?: defaultSeasonName(seasonNumber),
            episodes = episodes
                .sortedBy { it.episodeNumber }
                .map { episode ->
                    EpisodeView(
                        number = episode.episodeNumber,
                        name = episode.name,
                        overview = episode.overview,
                        imdbRating = omdbRatingsByEpisode[episode.episodeNumber],
                        tmdbRating = episode.voteAverage?.takeIf { it > 0 },
                        airDate = episode.airDate,
                    )
                },
        )

    private fun defaultSeasonName(seasonNumber: Int): String =
        if (seasonNumber == 0) "Specials" else "Season $seasonNumber"

    private fun seasonSortKey(seasonNumber: Int): Int =
        if (seasonNumber == 0) Int.MAX_VALUE else seasonNumber

    private fun resolveShowRating(
        omdbShowRating: String?,
        showVoteAverage: Double?,
        seasons: List<SeasonView>,
    ): Pair<Double?, RatingSource?> {
        OmdbParsing.parseRating(omdbShowRating)?.let { return it to RatingSource.IMDB }

        val episodeRatings = seasons.flatMap { season ->
            season.episodes.mapNotNull { it.resolvedRating() }
        }
        if (episodeRatings.isNotEmpty()) {
            return RatingResolver.average(episodeRatings)
        }

        showVoteAverage?.takeIf { it > 0 }?.let { return it to RatingSource.TMDB }
        return null to null
    }
}

class SeriesNotFoundException(val tmdbId: Int) : RuntimeException("Series not found.")

class SeriesDetailException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
