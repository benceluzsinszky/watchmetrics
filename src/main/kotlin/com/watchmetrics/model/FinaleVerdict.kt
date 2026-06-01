package com.watchmetrics.model

import java.util.Locale

enum class FinaleVerdict {
    ENDED_WELL,
    ENDED_BADLY,
    MIXED,
    ONGOING,
    UNKNOWN,
}

data class FinaleVerdictResult(
    val verdict: FinaleVerdict,
    val message: String,
) {
    val showInSearch: Boolean
        get() = verdict == FinaleVerdict.ENDED_WELL || verdict == FinaleVerdict.ENDED_BADLY

    val badgeClass: String
        get() = when (verdict) {
            FinaleVerdict.ENDED_WELL -> "bg-emerald-500/15 text-emerald-400"
            FinaleVerdict.ENDED_BADLY -> "bg-red-500/15 text-red-400"
            else -> ""
        }
}

object FinaleVerdictEvaluator {
    private val endedStatuses = setOf("Ended", "Canceled")

    fun evaluate(status: String?, highlights: SeriesHighlightsView): FinaleVerdictResult? {
        if (status != null && status !in endedStatuses) {
            return null
        }

        val finale = highlights.finale ?: return null
        val episodeRating = finale.lastEpisodeRating
        val seasonAverage = finale.lastSeasonAverage

        if (episodeRating == null && seasonAverage == null) {
            return null
        }

        val endedWell = isEndedWell(episodeRating, seasonAverage)
        val endedBadly = isEndedBadly(episodeRating, seasonAverage)

        val verdict = when {
            endedWell && !endedBadly -> FinaleVerdict.ENDED_WELL
            endedBadly && !endedWell -> FinaleVerdict.ENDED_BADLY
            else -> FinaleVerdict.MIXED
        }

        if (verdict == FinaleVerdict.MIXED) {
            return null
        }

        return FinaleVerdictResult(
            verdict = verdict,
            message = formatMessage(verdict, finale),
        )
    }

    private fun isEndedWell(episodeRating: Double?, seasonAverage: Double?): Boolean =
        when {
            episodeRating != null && seasonAverage != null ->
                episodeRating >= 8.0 && seasonAverage >= 7.5
            episodeRating != null -> episodeRating >= 8.0
            seasonAverage != null -> seasonAverage >= 8.0
            else -> false
        }

    private fun isEndedBadly(episodeRating: Double?, seasonAverage: Double?): Boolean =
        when {
            episodeRating != null && seasonAverage != null ->
                episodeRating < 7.0 || seasonAverage < 6.5
            episodeRating != null -> episodeRating < 7.0
            seasonAverage != null -> seasonAverage < 6.5
            else -> false
        }

    private fun formatMessage(verdict: FinaleVerdict, finale: SeriesFinaleHighlight): String {
        val ratingSuffix = formatRatingSuffix(finale)
        return when (verdict) {
            FinaleVerdict.ENDED_WELL -> "Ended well$ratingSuffix"
            FinaleVerdict.ENDED_BADLY -> "Weak ending$ratingSuffix"
            else -> ""
        }
    }

    private fun formatRatingSuffix(finale: SeriesFinaleHighlight): String {
        finale.lastEpisodeRating?.let { rating ->
            return " · finale ★ ${formatRating(rating)}"
        }
        finale.lastSeasonAverage?.let { average ->
            return " · final season ★ ${formatRating(average)} avg"
        }
        return ""
    }

    private fun formatRating(rating: Double): String =
        "%.1f".format(Locale.US, rating)
}
