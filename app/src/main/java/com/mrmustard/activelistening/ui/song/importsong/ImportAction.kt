package com.mrmustard.activelistening.ui.song.importsong

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R

@Composable
fun ImportAction(
    isImporting: Boolean,
    hasSong: Boolean,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onImportClick,
            enabled = !isImporting,
        ) {
            Text(
                if (hasSong) {
                    stringResource(R.string.import_song_change_song)
                } else {
                    stringResource(R.string.import_song_import_song)
                },
            )
        }
        if (isImporting) {
            Spacer(Modifier.width(16.dp))
            CircularProgressIndicator()
        }
    }
}
