package com.mrmustard.activelistening.data.importing

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.mrmustard.activelistening.domain.importsong.ImportSongError
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.importsong.SongImportGateway
import com.mrmustard.activelistening.domain.importsong.SongImportResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AndroidSongImportGateway @Inject constructor(
    @ApplicationContext private val context: Context,
    private val validator: SongImportValidator,
) : SongImportGateway {

    override suspend fun importSong(uri: String): SongImportResult = withContext(Dispatchers.IO) {
        runCatching {
            val androidUri = Uri.parse(uri)
            val displayName = resolveDisplayName(androidUri)
            val mimeType = context.contentResolver.getType(androidUri)

            validator.validateFormat(mimeType, displayName)?.let { error ->
                return@withContext SongImportResult.Error(error)
            }

            if (!canOpen(androidUri)) {
                return@withContext SongImportResult.Error(ImportSongError.UnreadableFile)
            }

            val metadata = readMetadata(androidUri)
                ?: return@withContext SongImportResult.Error(ImportSongError.UnreadableFile)

            validator.validateDuration(metadata.durationMillis)?.let { error ->
                return@withContext SongImportResult.Error(error)
            }

            SongImportResult.Success(
                ImportedSong(
                    uri = uri,
                    displayName = displayName,
                    mimeType = mimeType,
                    durationMillis = metadata.durationMillis,
                    title = metadata.title ?: displayName.substringBeforeLast('.'),
                    artist = metadata.artist,
                    artwork = metadata.artwork,
                ),
            )
        }.getOrElse {
            SongImportResult.Error(ImportSongError.UnreadableFile)
        }
    }

    private fun resolveDisplayName(uri: Uri): String {
        val resolver = context.contentResolver
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }?.let { return it }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            ?: DEFAULT_IMPORTED_SONG_NAME
    }

    private fun canOpen(uri: Uri): Boolean =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.read()
            } != null
        }.getOrDefault(false)

    private fun readMetadata(uri: Uri): SongMetadata? =
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, uri)
                val durationMillis = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?.takeIf { it > 0L }
                    ?: return@use null
                SongMetadata(
                    durationMillis = durationMillis,
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        ?.takeIf { it.isNotBlank() },
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        ?.takeIf { it.isNotBlank() },
                    artwork = retriever.embeddedPicture,
                )
            }
        }.getOrNull()

    private data class SongMetadata(
        val durationMillis: Long,
        val title: String?,
        val artist: String?,
        val artwork: ByteArray?,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SongMetadata

            if (durationMillis != other.durationMillis) return false
            if (title != other.title) return false
            if (artist != other.artist) return false
            if (!artwork.contentEquals(other.artwork)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = durationMillis.hashCode()
            result = 31 * result + (title?.hashCode() ?: 0)
            result = 31 * result + (artist?.hashCode() ?: 0)
            result = 31 * result + (artwork?.contentHashCode() ?: 0)
            return result
        }
    }

    private companion object {
        const val DEFAULT_IMPORTED_SONG_NAME = "Cancion importada"
    }
}
