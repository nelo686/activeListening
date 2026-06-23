package com.mrmustard.activelistening.data.session

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.mrmustard.activelistening.domain.session.SongArtworkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidSongArtworkRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SongArtworkRepository {
    override suspend fun load(songKey: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, songKey.toUri())
                retriever.embeddedPicture
            }
        }.getOrNull()
    }
}
