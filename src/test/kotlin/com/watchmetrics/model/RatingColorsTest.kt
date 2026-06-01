package com.watchmetrics.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RatingColorsTest {

    @Test
    fun `returns empty color for missing ratings`() {
        assertEquals("#1e293b", RatingColors.forImdb(null))
        assertEquals("#1e293b", RatingColors.forImdb(0.0))
    }

    @Test
    fun `maps five to red and ten to green`() {
        assertEquals("hsl(0, 72%, 36%)", RatingColors.forImdb(5.0))
        assertEquals("hsl(120, 80%, 42%)", RatingColors.forImdb(10.0))
    }

    @Test
    fun `uses harsh colors below five`() {
        val veryLow = RatingColors.forImdb(1.0)
        val low = RatingColors.forImdb(2.0)
        val weak = RatingColors.forImdb(4.0)
        val baseline = RatingColors.forImdb(5.0)

        assertTrue(extractLightness(veryLow) < extractLightness(low))
        assertTrue(extractLightness(low) < extractLightness(weak))
        assertTrue(extractLightness(weak) < extractLightness(baseline))
        assertTrue(extractSaturation(veryLow) < extractSaturation(baseline))
        assertEquals(0, extractHue(baseline))
        assertTrue(extractHue(RatingColors.forImdb(10.0)) >= 120)
    }

    private fun extractHue(color: String): Int =
        color.removePrefix("hsl(").substringBefore(",").toInt()

    private fun extractSaturation(color: String): Int =
        color.substringAfter(", ").substringBefore("%").toInt()

    private fun extractLightness(color: String): Int =
        color.substringAfterLast(", ").removeSuffix("%)").toInt()
}
