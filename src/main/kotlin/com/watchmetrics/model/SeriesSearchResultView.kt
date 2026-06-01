package com.watchmetrics.model

data class SeriesSearchPageView(
    val results: List<SeriesSearchResultView>,
    val totalResults: Int,
)

data class SeriesSearchResultView(
    val id: Int,
    val name: String,
    val overview: String?,
    val posterUrl: String?,
    val firstAirYear: String?,
    val finaleVerdict: FinaleVerdictResult?,
) {
    companion object {
        fun from(summary: TmdbTvShowSummary, finaleVerdict: FinaleVerdictResult?): SeriesSearchResultView =
            SeriesSearchResultView(
                id = summary.id,
                name = summary.name,
                overview = summary.overview,
                posterUrl = summary.posterUrl,
                firstAirYear = summary.firstAirDate?.take(4),
                finaleVerdict = finaleVerdict,
            )
    }
}
