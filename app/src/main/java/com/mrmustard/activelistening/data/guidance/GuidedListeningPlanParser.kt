package com.mrmustard.activelistening.data.guidance

import com.mrmustard.activelistening.domain.guidance.GuidedListeningMarkerSuggestion
import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence

object GuidedListeningPlanParser {

    fun parse(content: String): List<GuidedListeningMarkerSuggestion> =
        content.lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .mapNotNull(::parseLine)
            .toList()

    private fun parseLine(line: String): GuidedListeningMarkerSuggestion? {
        val parts = line.split("|").map(String::trim)
        if (parts.size < 3) return null

        val id = parts[0].toIntOrNull() ?: return null
        val title = parts[1].removePrefix("TITULO:").trim()
        val prompt = if (parts.size >= 4) {
            parts[2]
        } else {
            parts.drop(2).joinToString(separator = " | ")
        }
            .removePrefix("PISTA:")
            .trim()
        val musicalContrast = parts.getOrNull(3)
            ?.removePrefix("CONTRASTE:")
            ?.trim()
            ?.toMusicalContrast()

        if (title.isBlank() || prompt.isBlank()) return null
        return GuidedListeningMarkerSuggestion(
            id = id,
            title = title,
            prompt = prompt,
            musicalContrast = musicalContrast,
        )
    }

    private fun String.toMusicalContrast(): SectionMusicalContrast? {
        if (isBlank()) return null
        val normalized = lowercase().trim(' ', '.', ',', ';', ':')
        if (
            normalized == "no" ||
            normalized == "sin contraste" ||
            normalized == "sin cambio" ||
            normalized == "ninguno"
        ) {
            return null
        }
        return SectionMusicalContrast(
            confidence = SectionRhythmConfidence.Low,
            explanation = this,
        )
    }
}
