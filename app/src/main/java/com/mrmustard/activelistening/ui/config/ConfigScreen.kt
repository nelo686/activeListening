package com.mrmustard.activelistening.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.ui.song.structure.toDisplayName
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    learningLevel: LearningLevel,
    guidanceIntensity: GuidanceIntensity,
    onLearningLevelSelected: (LearningLevel) -> Unit,
    onGuidanceIntensitySelected: (GuidanceIntensity) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                item {
                    SettingsSection(
                        title = stringResource(R.string.section_learning_level_title),
                        description = stringResource(R.string.settings_learning_level_description),
                    ) {
                        LearningLevel.entries.forEach { level ->
                            FilterChip(
                                selected = learningLevel == level,
                                onClick = { onLearningLevelSelected(level) },
                                label = { Text(level.toDisplayName()) },
                            )
                        }
                    }
                }

                item {
                    SettingsSection(
                        title = stringResource(R.string.guidance_intensity_title),
                        description = stringResource(R.string.settings_guidance_intensity_description),
                    ) {
                        GuidanceIntensity.entries.forEach { intensity ->
                            FilterChip(
                                selected = guidanceIntensity == intensity,
                                onClick = { onGuidanceIntensitySelected(intensity) },
                                label = { Text(intensity.toDisplayName()) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfigScreenPreview() {
    ActiveListeningTheme {
        ConfigScreen(
            learningLevel = LearningLevel.Intermediate,
            guidanceIntensity = GuidanceIntensity.Normal,
            onLearningLevelSelected = {},
            onGuidanceIntensitySelected = {},
            onBackClick = {},
        )
    }
}
