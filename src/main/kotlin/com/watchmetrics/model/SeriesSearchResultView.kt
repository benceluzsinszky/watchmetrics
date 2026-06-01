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
    val loadVerdict: Boolean,
) {
    companion object {
        fun from(summary: TmdbTvShowSummary, loadVerdict: Boolean = false): SeriesSearchResultView =
            SeriesSearchResultView(
                id = summary.id,
                name = summary.name,
                overview = summary.overview,
                posterUrl = summary.posterUrl,
                firstAirYear = summary.firstAirDate?.take(4),
                loadVerdict = loadVerdict,
            )
    }
}
