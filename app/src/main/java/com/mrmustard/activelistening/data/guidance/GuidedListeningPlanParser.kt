package com.mrmustard.activelistening.data.guidance

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
        val prompt = parts.drop(2).joinToString(separator = " | ")
            .removePrefix("PISTA:")
            .trim()

        if (title.isBlank() || prompt.isBlank()) return null
        return GuidedListeningMarkerSuggestion(
            id = id,
            title = title,
            prompt = prompt,
        )
    }
}

