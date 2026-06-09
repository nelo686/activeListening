package com.mrmustard.activelistening.data.guidance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidedListeningPlanParserTest {

    @Test
    fun `parses marker suggestions from pipe separated lines`() {
        val content = """
            0|Inicio atento|Escucha como aparece el pulso principal.
            1|Posible transicion|Compara ritmo, energia e instrumentacion.
        """.trimIndent()

        val result = GuidedListeningPlanParser.parse(content)

        assertEquals(2, result.size)
        assertEquals(0, result[0].id)
        assertEquals("Inicio atento", result[0].title)
        assertEquals("Escucha como aparece el pulso principal.", result[0].prompt)
    }

    @Test
    fun `parses optional rhythm contrast from fourth field`() {
        val content = """
            0|Verso|Compara el pulso con la intro.|Posible cambio de feel: parece mas estable que la entrada, no solo mas energia.
            1|Coro|Escucha si se repite la idea central.|sin contraste
        """.trimIndent()

        val result = GuidedListeningPlanParser.parse(content)

        assertEquals(2, result.size)
        assertEquals(
            "Posible cambio de feel: parece mas estable que la entrada, no solo mas energia.",
            result[0].musicalContrast?.explanation,
        )
        assertEquals(null, result[1].musicalContrast)
    }

    @Test
    fun `ignores malformed lines`() {
        val content = """
            Esto no cumple el formato
            2|Cierre|Identifica si hay outro o corte seco.
        """.trimIndent()

        val result = GuidedListeningPlanParser.parse(content)

        assertEquals(1, result.size)
        assertEquals(2, result.single().id)
    }

    @Test
    fun `returns empty list when no valid suggestions exist`() {
        val result = GuidedListeningPlanParser.parse("sin formato")

        assertTrue(result.isEmpty())
    }
}
