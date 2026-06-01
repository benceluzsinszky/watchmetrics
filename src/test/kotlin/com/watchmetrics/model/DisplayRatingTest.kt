package com.watchmetrics.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DisplayRatingTest {

    @Test
    fun `prefers imdb over tmdb`() {
        val rating = RatingResolver.from(imdb = 8.0, tmdb = 7.0)

        assertNotNull(rating)
        assertEquals(RatingSource.IMDB, rating!!.source)
        assertEquals("★ 8.0 IMDb", rating.formatted)
    }

    @Test
    fun `falls back to tmdb when imdb missing`() {
        val rating = RatingResolver.from(imdb = null, tmdb = 7.5)

        assertNotNull(rating)
        assertEquals(RatingSource.TMDB, rating!!.source)
        assertEquals("★ 7.5 TMDB", rating.formatted)
    }
}
