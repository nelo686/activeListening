package com.mrmustard.activelistening.domain.structure

object SongStructureFactory {

    fun createInitialSections(durationMillis: Long): List<SongSection> {
        if (durationMillis <= 0L) return emptyList()

        val boundaries = buildSet {
            add(0L)
            add((durationMillis * 0.25f).toLong())
            add((durationMillis * 0.5f).toLong())
            add((durationMillis * 0.75f).toLong())
            add(durationMillis)
        }.map { it.coerceIn(0L, durationMillis) }
            .distinct()
            .sorted()

        return boundaries.zipWithNext()
            .filter { (start, end) -> end - start >= MIN_SECTION_DURATION_MILLIS }
            .mapIndexed { index, (start, end) ->
                val label = DEFAULT_LABELS.getOrElse(index) { SectionLabel.Other }
                SongSection(
                    id = index,
                    startMillis = start,
                    endMillis = end,
                    label = label,
                    prompt = label.defaultPrompt(),
                    isApproximate = true,
                )
            }.ifEmpty {
                listOf(
                    SongSection(
                        id = 0,
                        startMillis = 0L,
                        endMillis = durationMillis,
                        label = SectionLabel.Other,
                        prompt = SectionLabel.Other.defaultPrompt(),
                        isApproximate = true,
                    ),
                )
            }
    }

    fun activeSectionId(
        sections: List<SongSection>,
        positionMillis: Long,
    ): Int? {
        if (sections.isEmpty()) return null
        return sections.firstOrNull { section ->
            positionMillis >= section.startMillis && positionMillis < section.endMillis
        }?.id ?: sections.lastOrNull { positionMillis >= it.endMillis }?.id ?: sections.first().id
    }

    fun adjustSectionBoundary(
        sections: List<SongSection>,
        sectionId: Int,
        boundary: SectionBoundary,
        deltaMillis: Long,
    ): List<SongSection> {
        val index = sections.indexOfFirst { it.id == sectionId }
        if (index == -1 || deltaMillis == 0L) return sections

        return when (boundary) {
            SectionBoundary.Start -> adjustStartBoundary(sections, index, deltaMillis)
            SectionBoundary.End -> adjustEndBoundary(sections, index, deltaMillis)
        }
    }

    private fun adjustStartBoundary(
        sections: List<SongSection>,
        index: Int,
        deltaMillis: Long,
    ): List<SongSection> {
        if (index == 0) return sections

        val previous = sections[index - 1]
        val current = sections[index]
        val newStart = (current.startMillis + deltaMillis).coerceIn(
            previous.startMillis + MIN_SECTION_DURATION_MILLIS,
            current.endMillis - MIN_SECTION_DURATION_MILLIS,
        )

        if (newStart == current.startMillis) return sections
        return sections.mapIndexed { currentIndex, section ->
            when (currentIndex) {
                index - 1 -> previous.copy(endMillis = newStart, isApproximate = true)
                index -> current.copy(startMillis = newStart, isApproximate = true)
                else -> section
            }
        }
    }

    private fun adjustEndBoundary(
        sections: List<SongSection>,
        index: Int,
        deltaMillis: Long,
    ): List<SongSection> {
        if (index == sections.lastIndex) return sections

        val current = sections[index]
        val next = sections[index + 1]
        val newEnd = (current.endMillis + deltaMillis).coerceIn(
            current.startMillis + MIN_SECTION_DURATION_MILLIS,
            next.endMillis - MIN_SECTION_DURATION_MILLIS,
        )

        if (newEnd == current.endMillis) return sections
        return sections.mapIndexed { currentIndex, section ->
            when (currentIndex) {
                index -> current.copy(endMillis = newEnd, isApproximate = true)
                index + 1 -> next.copy(startMillis = newEnd, isApproximate = true)
                else -> section
            }
        }
    }

    private fun SectionLabel.defaultPrompt(): String =
        when (this) {
            SectionLabel.Intro -> "Escucha como entra la cancion: pulso, energia e instrumentos principales."
            SectionLabel.Verse -> "Compara esta parte con el inicio y busca si aparece una idea vocal o ritmica estable."
            SectionLabel.Chorus -> "Pregunta guia: sube la energia, se repite una frase central o cambia la instrumentacion?"
            SectionLabel.Bridge -> "Busca contraste: otro patron, una pausa, un puente o una preparacion hacia el cierre."
            SectionLabel.Outro -> "Identifica como termina: outro, corte seco, repeticion o bajada de energia."
            SectionLabel.Other -> "Escucha esta seccion y decide que funcion musical cumple dentro de la cancion."
        }

    private val DEFAULT_LABELS = listOf(
        SectionLabel.Intro,
        SectionLabel.Verse,
        SectionLabel.Chorus,
        SectionLabel.Outro,
    )

    const val MIN_SECTION_DURATION_MILLIS = 5_000L
}
