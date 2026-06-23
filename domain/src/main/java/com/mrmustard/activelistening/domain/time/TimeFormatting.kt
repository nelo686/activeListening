package com.mrmustard.activelistening.domain.time

fun formatTimeCode(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0L) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

fun parseTimeCode(input: String): Long? {
    val parts = input.trim().split(":")
    if (parts.size != 2) return null
    val minutes = parts[0].toLongOrNull() ?: return null
    val seconds = parts[1].toLongOrNull() ?: return null
    if (minutes < 0L || seconds !in 0L..59L) return null
    return minutes * 60_000L + seconds * 1_000L
}
