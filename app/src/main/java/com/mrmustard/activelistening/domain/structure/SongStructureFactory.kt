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

    fun setSectionBoundary(
        sections: List<SongSection>,
        sectionId: Int,
        boundary: SectionBoundary,
        positionMillis: Long,
    ): List<SongSection> {
        val index = sections.indexOfFirst { it.id == sectionId }
        if (index == -1) return sections

        return when (boundary) {
            SectionBoundary.Start -> setStartBoundary(sections, index, positionMillis)
            SectionBoundary.End -> setEndBoundary(sections, index, positionMillis)
        }
    }

    fun splitSectionAt(
        sections: List<SongSection>,
        positionMillis: Long,
    ): List<SongSection> {
        val index = sections.indexOfFirst { section ->
            positionMillis > section.startMillis && positionMillis < section.endMillis
        }
        if (index == -1) return sections

        val section = sections[index]
        if (positionMillis - section.startMillis < MIN_SECTION_DURATION_MILLIS ||
            section.endMillis - positionMillis < MIN_SECTION_DURATION_MILLIS
        ) {
            return sections
        }

        val nextId = (sections.maxOfOrNull { it.id } ?: -1) + 1
        val left = section.copyWithTiming(endMillis = positionMillis)
        val right = SongSection(
            id = nextId,
            startMillis = positionMillis,
            endMillis = section.endMillis,
            label = SectionLabel.Other,
            status = SectionStatus.Suggested,
            prompt = SectionLabel.Other.defaultPrompt(),
            isApproximate = true,
            rhythmInfo = SectionRhythmEstimator.estimate(section.endMillis - positionMillis),
            musicalContrast = section.musicalContrast?.copy(confidence = SectionRhythmConfidence.Low),
        )

        return sections.flatMapIndexed { currentIndex, currentSection ->
            if (currentIndex == index) listOf(left, right) else listOf(currentSection)
        }
    }

    fun removeBoundaryAfter(
        sections: List<SongSection>,
        sectionId: Int,
    ): List<SongSection> {
        val index = sections.indexOfFirst { it.id == sectionId }
        if (index == -1 || index == sections.lastIndex) return sections

        val current = sections[index]
        val next = sections[index + 1]
        val merged = current.copyWithTiming(endMillis = next.endMillis)

        return sections.filterIndexed { currentIndex, _ ->
            currentIndex != index + 1
        }.mapIndexed { currentIndex, section ->
            if (currentIndex == index) merged else section
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
                index - 1 -> previous.copyWithTiming(endMillis = newStart)
                index -> current.copyWithTiming(startMillis = newStart)
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
                index -> current.copyWithTiming(endMillis = newEnd)
                index + 1 -> next.copyWithTiming(startMillis = newEnd)
                else -> section
            }
        }
    }

    private fun setStartBoundary(
        sections: List<SongSection>,
        index: Int,
        positionMillis: Long,
    ): List<SongSection> {
        if (index == 0) return sections

        val previous = sections[index - 1]
        val current = sections[index]
        val newStart = positionMillis.coerceIn(
            previous.startMillis + MIN_SECTION_DURATION_MILLIS,
            current.endMillis - MIN_SECTION_DURATION_MILLIS,
        )

        if (newStart == current.startMillis) return sections
        return sections.mapIndexed { currentIndex, section ->
            when (currentIndex) {
                index - 1 -> previous.copyWithTiming(endMillis = newStart)
                index -> current.copyWithTiming(startMillis = newStart)
                else -> section
            }
        }
    }

    private fun setEndBoundary(
        sections: List<SongSection>,
        index: Int,
        positionMillis: Long,
    ): List<SongSection> {
        if (index == sections.lastIndex) return sections

        val current = sections[index]
        val next = sections[index + 1]
        val newEnd = positionMillis.coerceIn(
            current.startMillis + MIN_SECTION_DURATION_MILLIS,
            next.endMillis - MIN_SECTION_DURATION_MILLIS,
        )

        if (newEnd == current.endMillis) return sections
        return sections.mapIndexed { currentIndex, section ->
            when (currentIndex) {
                index -> current.copyWithTiming(endMillis = newEnd)
                index + 1 -> next.copyWithTiming(startMillis = newEnd)
                else -> section
            }
        }
    }

    private fun SectionLabel.defaultPrompt(): String =
        when (this) {
            SectionLabel.Intro -> "Escucha como entra la cancion: cambia la energia, el ritmo o la instrumentacion desde el primer pulso?"
            SectionLabel.Verse -> "Compara esta parte con el inicio: se repite una idea vocal o cambia la sensacion del acompanamiento?"
            SectionLabel.Chorus -> "Pregunta guia: sube la energia, se repite una frase central o cambia la instrumentacion?"
            SectionLabel.Bridge -> "Busca contraste: cambia el patron ritmico, aparece otra textura o funciona como transicion?"
            SectionLabel.Outro -> "Identifica como termina: repeticion, corte seco, bajada de energia o cambio de instrumentacion?"
            SectionLabel.Other -> "Escucha esta seccion: que cambia en energia, repeticion, ritmo, instrumentacion o sensacion general?"
        }

    private val DEFAULT_LABELS = listOf(
        SectionLabel.Intro,
        SectionLabel.Verse,
        SectionLabel.Chorus,
        SectionLabel.Outro,
    )

    const val MIN_SECTION_DURATION_MILLIS = 5_000L

    private fun SongSection.copyWithTiming(
        startMillis: Long = this.startMillis,
        endMillis: Long = this.endMillis,
    ): SongSection =
        copy(
            startMillis = startMillis,
            endMillis = endMillis,
            isApproximate = true,
            rhythmInfo = SectionRhythmEstimator.estimate(endMillis - startMillis),
            musicalContrast = musicalContrast?.copy(confidence = SectionRhythmConfidence.Low),
        )
}
