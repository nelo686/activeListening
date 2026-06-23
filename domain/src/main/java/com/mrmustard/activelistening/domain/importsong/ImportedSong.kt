package com.mrmustard.activelistening.domain.importsong

data class ImportedSong(
    val uri: String,
    val displayName: String,
    val mimeType: String?,
    val durationMillis: Long,
    val title: String = displayName,
    val artist: String? = null,
    val artwork: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is ImportedSong &&
            uri == other.uri &&
            displayName == other.displayName &&
            mimeType == other.mimeType &&
            durationMillis == other.durationMillis &&
            title == other.title &&
            artist == other.artist &&
            artwork.contentEquals(other.artwork)

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + durationMillis.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + artwork.contentHashCode()
        return result
    }
}
