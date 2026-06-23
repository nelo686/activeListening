package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import com.mrmustard.activelistening.domain.structure.SectionRhythmInfo
import com.mrmustard.activelistening.domain.structure.SectionRhythmRegularity

@Composable
internal fun RhythmInfoPanel(
    rhythmInfo: SectionRhythmInfo?,
    musicalContrast: SectionMusicalContrast?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.structure_rhythm_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            when {
                rhythmInfo?.regularity == SectionRhythmRegularity.Irregular -> Text(
                    stringResource(R.string.structure_rhythm_irregular),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                rhythmInfo?.estimatedBars != null -> Text(
                    stringResource(R.string.structure_rhythm_estimated_bars, rhythmInfo.estimatedBars),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> Text(
                    stringResource(R.string.structure_rhythm_unavailable),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (rhythmInfo?.confidence == SectionRhythmConfidence.Low &&
                rhythmInfo.regularity != SectionRhythmRegularity.Irregular
            ) {
                Text(
                    stringResource(R.string.structure_rhythm_low_confidence),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            musicalContrast?.let { contrast ->
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                Text(
                    stringResource(R.string.structure_contrast_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(contrast.explanation, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (contrast.confidence == SectionRhythmConfidence.Low) {
                    Text(
                        stringResource(R.string.structure_warning_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(R.string.structure_contrast_low_confidence),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
