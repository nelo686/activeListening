package com.mrmustard.activelistening.data.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextWrappingTest {

    @Test
    fun `wraps text using measured width`() {
        val lines = wrapTextToWidth(
            text = "Cambio ritmico claramente perceptible",
            maxWidth = 16f,
            measureText = { it.length.toFloat() },
        )

        assertEquals(
            listOf("Cambio ritmico", "claramente", "perceptible"),
            lines,
        )
        assertTrue(lines.all { it.length <= 16 })
    }

    @Test
    fun `splits a single word longer than available width`() {
        val lines = wrapTextToWidth(
            text = "supercalifragilistico",
            maxWidth = 6f,
            measureText = { it.length.toFloat() },
        )

        assertEquals(listOf("superc", "alifra", "gilist", "ico"), lines)
        assertTrue(lines.all { it.length <= 6 })
    }

    @Test
    fun `preserves explicit paragraph breaks`() {
        val lines = wrapTextToWidth(
            text = "Primera linea\nSegunda linea",
            maxWidth = 20f,
            measureText = { it.length.toFloat() },
        )

        assertEquals(listOf("Primera linea", "Segunda linea"), lines)
    }
}
