package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionStatus

@Composable
fun SectionLabel.toDisplayName(): String =
    when (this) {
        SectionLabel.Intro -> stringResource(R.string.section_label_intro)
        SectionLabel.Verse -> stringResource(R.string.section_label_verse)
        SectionLabel.Chorus -> stringResource(R.string.section_label_chorus)
        SectionLabel.Bridge -> stringResource(R.string.section_label_bridge)
        SectionLabel.Outro -> stringResource(R.string.section_label_outro)
        SectionLabel.Other -> stringResource(R.string.section_label_other)
    }

@Composable
fun SectionStatus.toDisplayName(): String =
    when (this) {
        SectionStatus.Suggested -> stringResource(R.string.section_status_suggested)
        SectionStatus.Confirmed -> stringResource(R.string.section_status_confirmed)
        SectionStatus.Uncertain -> stringResource(R.string.section_status_uncertain)
    }

fun formatSectionTime(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0L) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
