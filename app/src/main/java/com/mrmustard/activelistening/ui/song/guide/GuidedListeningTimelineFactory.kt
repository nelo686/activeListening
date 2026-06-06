package com.mrmustard.activelistening.ui.song.guide

object GuidedListeningTimelineFactory {

    fun create(durationMillis: Long): List<GuidedListeningMarker> {
        if (durationMillis <= 0L) return emptyList()

        val finalListeningPoint = (durationMillis - FINAL_POINT_OFFSET_MILLIS)
            .coerceAtLeast(0L)

        return listOf(
            MarkerSeed(
                fraction = 0f,
                title = "Inicio",
                prompt = "Escucha como entra la cancion: ritmo, energia e instrumentos principales.",
            ),
            MarkerSeed(
                fraction = 0.25f,
                title = "Primer cambio",
                prompt = "Pregunta guia: notas algun cambio de patron, intensidad o instrumentacion?",
            ),
            MarkerSeed(
                fraction = 0.5f,
                title = "Mitad",
                prompt = "Compara este punto con el inicio. Sigue la misma seccion o aparece una nueva idea?",
            ),
            MarkerSeed(
                fraction = 0.75f,
                title = "Ultimo tramo",
                prompt = "Presta atencion a repeticiones, puente, solo o preparacion del cierre.",
            ),
            MarkerSeed(
                positionMillis = finalListeningPoint,
                title = "Cierre",
                prompt = "Identifica como termina: outro, corte seco, repeticion o bajada de energia.",
            ),
        ).mapIndexed { index, seed ->
            GuidedListeningMarker(
                id = index,
                positionMillis = seed.resolvePosition(durationMillis),
                title = seed.title,
                prompt = seed.prompt,
            )
        }.distinctBy { it.positionMillis }
            .sortedBy { it.positionMillis }
            .mapIndexed { index, marker -> marker.copy(id = index) }
    }

    private data class MarkerSeed(
        val title: String,
        val prompt: String,
        val fraction: Float? = null,
        val positionMillis: Long? = null,
    ) {
        fun resolvePosition(durationMillis: Long): Long =
            positionMillis ?: (durationMillis * requireNotNull(fraction)).toLong()
    }

    private const val FINAL_POINT_OFFSET_MILLIS = 10_000L
}

