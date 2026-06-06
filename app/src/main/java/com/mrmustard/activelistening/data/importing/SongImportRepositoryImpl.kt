package com.mrmustard.activelistening.data.importing

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.ImportSongError
import com.mrmustard.activelistening.domain.ImportedSong
import com.mrmustard.activelistening.domain.SongImportResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SongImportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val validator: SongImportValidator,
) : SongImportRepository {

    override suspend fun importSong(uri: Uri): SongImportResult = withContext(Dispatchers.IO) {
        runCatching {
            val displayName = resolveDisplayName(uri)
            val mimeType = context.contentResolver.getType(uri)

            validator.validateFormat(mimeType, displayName)?.let { error ->
                return@withContext SongImportResult.Error(error)
            }

            if (!canOpen(uri)) {
                return@withContext SongImportResult.Error(ImportSongError.UnreadableFile)
            }

            val durationMillis = readDurationMillis(uri)
                ?: return@withContext SongImportResult.Error(ImportSongError.UnreadableFile)

            validator.validateDuration(durationMillis)?.let { error ->
                return@withContext SongImportResult.Error(error)
            }

            SongImportResult.Success(
                ImportedSong(
                    uri = uri,
                    displayName = displayName,
                    mimeType = mimeType,
                    durationMillis = durationMillis,
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
            ?: context.getString(R.string.imported_song_default_name)
    }

    private fun canOpen(uri: Uri): Boolean =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.read()
            } != null
        }.getOrDefault(false)

    private fun readDurationMillis(uri: Uri): Long? =
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?.takeIf { it > 0L }
            }
        }.getOrNull()
}
