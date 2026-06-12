package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.time.formatTimeCode

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
fun SongSection.toDisplayName(): String =
    customLabel?.takeIf { label == SectionLabel.Other && it.isNotBlank() }
        ?: label.toDisplayName()

@Composable
fun SectionStatus.toDisplayName(): String =
    when (this) {
        SectionStatus.Suggested -> stringResource(R.string.section_status_suggested)
        SectionStatus.Confirmed -> stringResource(R.string.section_status_confirmed)
        SectionStatus.Uncertain -> stringResource(R.string.section_status_uncertain)
    }

@Composable
fun LearningLevel.toDisplayName(): String =
    when (this) {
        LearningLevel.Introductory -> stringResource(R.string.learning_level_introductory)
        LearningLevel.Intermediate -> stringResource(R.string.learning_level_intermediate)
        LearningLevel.Advanced -> stringResource(R.string.learning_level_advanced)
        LearningLevel.Expert -> stringResource(R.string.learning_level_expert)
    }

@Composable
fun GuidanceIntensity.toDisplayName(): String =
    when (this) {
        GuidanceIntensity.Normal -> stringResource(R.string.guidance_intensity_normal)
        GuidanceIntensity.Reduced -> stringResource(R.string.guidance_intensity_reduced)
    }

fun formatSectionTime(millis: Long): String = formatTimeCode(millis)
