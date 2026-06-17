package com.mrmustard.activelistening.ui.config

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
            SettingsHeader(onBackClick = onBackClick)
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SettingsCard(
                        icon = R.drawable.ic_school_24,
                        title = stringResource(R.string.section_learning_level_title),
                        description = stringResource(R.string.settings_learning_level_description),
                    ) {
                        LearningLevelGrid(
                            selectedLevel = learningLevel,
                            onLevelSelected = onLearningLevelSelected,
                        )
                    }
                }
                item {
                    SettingsCard(
                        icon = R.drawable.ic_guidance_24,
                        title = stringResource(R.string.guidance_intensity_title),
                        description = stringResource(R.string.settings_guidance_intensity_description),
                    ) {
                        GuidanceIntensityColumn(
                            selectedIntensity = guidanceIntensity,
                            onIntensitySelected = onGuidanceIntensitySelected,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(stringResource(R.string.settings_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24),
                    contentDescription = stringResource(R.string.settings_back),
                )
            }
        },
        actions = {
            Icon(
                painter = painterResource(R.drawable.ic_settings_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp),
            )
        },
    )
}

@Composable
private fun SettingsCard(
    icon: Int,
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

@Composable
private fun LearningLevelGrid(
    selectedLevel: LearningLevel,
    onLevelSelected: (LearningLevel) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LearningLevel.entries.chunked(2).forEach { rowLevels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowLevels.forEach { level ->
                    LearningLevelButton(
                        modifier = Modifier.weight(1f),
                        level = level,
                        selected = selectedLevel == level,
                        onClick = { onLevelSelected(level) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningLevelButton(
    level: LearningLevel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        modifier = modifier
            .height(54.dp)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                    )
                } else {
                    Modifier
                },
            ),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.outline
            },
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        ),
    ) {
        Text(text = level.toDisplayName())
    }
}

@Composable
private fun GuidanceIntensityColumn(
    selectedIntensity: GuidanceIntensity,
    onIntensitySelected: (GuidanceIntensity) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GuidanceIntensityOption(
            intensity = GuidanceIntensity.Normal,
            selected = selectedIntensity == GuidanceIntensity.Normal,
            description = stringResource(R.string.settings_guidance_intensity_normal_description),
            onClick = { onIntensitySelected(GuidanceIntensity.Normal) },
        )
        GuidanceIntensityOption(
            intensity = GuidanceIntensity.Reduced,
            selected = selectedIntensity == GuidanceIntensity.Reduced,
            description = stringResource(R.string.settings_guidance_intensity_reduced_description),
            onClick = { onIntensitySelected(GuidanceIntensity.Reduced) },
        )
    }
}

@Composable
private fun GuidanceIntensityOption(
    intensity: GuidanceIntensity,
    selected: Boolean,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(8.dp),
            )
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.outline,
                ),
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, CircleShape),
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = intensity.toDisplayName(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
