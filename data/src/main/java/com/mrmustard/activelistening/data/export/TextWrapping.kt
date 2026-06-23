package com.mrmustard.activelistening.data.export

internal fun wrapTextToWidth(
    text: String,
    maxWidth: Float,
    measureText: (String) -> Float,
): List<String> {
    require(maxWidth > 0f)

    return text.split('\n').flatMap { paragraph ->
        paragraph.wrapParagraph(maxWidth, measureText)
    }
}

private fun String.wrapParagraph(
    maxWidth: Float,
    measureText: (String) -> Float,
): List<String> {
    if (isEmpty()) return listOf("")

    val lines = mutableListOf<String>()
    var currentLine = ""

    trim().split(Regex("\\s+")).forEach { word ->
        val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (measureText(candidate) <= maxWidth) {
            currentLine = candidate
            return@forEach
        }

        if (currentLine.isNotEmpty()) {
            lines += currentLine
            currentLine = ""
        }

        var remainingWord = word
        while (remainingWord.isNotEmpty() && measureText(remainingWord) > maxWidth) {
            val splitIndex = remainingWord.largestFittingPrefix(maxWidth, measureText)
            lines += remainingWord.take(splitIndex)
            remainingWord = remainingWord.drop(splitIndex)
        }
        currentLine = remainingWord
    }

    if (currentLine.isNotEmpty()) lines += currentLine
    return lines.ifEmpty { listOf("") }
}

private fun String.largestFittingPrefix(
    maxWidth: Float,
    measureText: (String) -> Float,
): Int {
    var low = 1
    var high = length
    var best = 1

    while (low <= high) {
        val middle = (low + high) / 2
        if (measureText(take(middle)) <= maxWidth) {
            best = middle
            low = middle + 1
        } else {
            high = middle - 1
        }
    }
    return best
}
