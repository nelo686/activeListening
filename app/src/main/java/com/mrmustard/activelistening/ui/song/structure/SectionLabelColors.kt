package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mrmustard.activelistening.domain.structure.SectionLabel

@Composable
internal fun SectionLabel.sectionColor(): Color = when (this) {
    SectionLabel.Intro -> Color(0xFFC9ADFF)
    SectionLabel.Verse -> Color(0xFFE3D8FF)
    SectionLabel.Chorus -> Color(0xFFD0C5DE)
    SectionLabel.Bridge -> Color(0xFFF0B1C4)
    SectionLabel.Outro -> Color(0xFFE91E63)
    SectionLabel.Other -> MaterialTheme.colorScheme.outline
}

@Composable
internal fun SectionLabel.onSectionColor(): Color = when (this) {
    SectionLabel.Outro -> Color.White
    SectionLabel.Other -> MaterialTheme.colorScheme.onSurface
    else -> Color(0xFF211A29)
}
