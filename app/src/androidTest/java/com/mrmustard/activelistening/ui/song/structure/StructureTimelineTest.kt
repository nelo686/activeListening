package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionMusicalContrast
import com.mrmustard.activelistening.domain.structure.SectionRhythmConfidence
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StructureTimelineTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rhythmChangeAppearsWhenSectionContrastIsEnabled() {
        composeRule.setContent {
            var sections by remember { mutableStateOf(listOf(section())) }
            ActiveListeningTheme {
                StructureTimeline(
                    sections = sections,
                    selectedSectionId = null,
                    activeSectionId = null,
                    positionMillis = 0L,
                    durationMillis = 30_000L,
                    onSectionClick = { sectionId ->
                        sections = sections.map { current ->
                            if (current.id == sectionId) {
                                current.copy(
                                    musicalContrast = SectionMusicalContrast(
                                        confidence = SectionRhythmConfidence.Low,
                                        explanation = "Cambio marcado manualmente",
                                    ),
                                )
                            } else {
                                current
                            }
                        }
                    },
                )
            }
        }

        composeRule.onAllNodesWithText("Cambio de ritmo").assertCountEquals(0)
        composeRule.onNodeWithText("Intro").performClick()
        composeRule.onAllNodesWithText("Cambio de ritmo").assertCountEquals(1)
    }

    @Test
    fun uncertainSectionShowsStatusBadge() {
        composeRule.setContent {
            ActiveListeningTheme {
                StructureTimeline(
                    sections = listOf(section().copy(status = SectionStatus.Uncertain)),
                    selectedSectionId = null,
                    activeSectionId = null,
                    positionMillis = 0L,
                    durationMillis = 30_000L,
                    onSectionClick = {},
                )
            }
        }

        composeRule.onAllNodesWithText("Dudosa").assertCountEquals(1)
        composeRule.onAllNodesWithText(
            "Desliza para explorar. Usa los bordes para ajustar.",
        ).assertCountEquals(1)
    }

    @Test
    fun draggingBoundaryReportsNewSharedPosition() {
        var changedSectionId: Int? = null
        var changedPositionMillis: Long? = null
        composeRule.setContent {
            ActiveListeningTheme {
                StructureTimeline(
                    sections = listOf(
                        section(),
                        section().copy(
                            id = 1,
                            startMillis = 30_000L,
                            endMillis = 60_000L,
                            label = SectionLabel.Verse,
                        ),
                    ),
                    selectedSectionId = null,
                    activeSectionId = null,
                    positionMillis = 0L,
                    durationMillis = 60_000L,
                    onSectionClick = {},
                    onBoundaryChanged = { sectionId, positionMillis ->
                        changedSectionId = sectionId
                        changedPositionMillis = positionMillis
                    },
                )
            }
        }

        composeRule.onNodeWithTag("structure_boundary_0").performTouchInput { swipeRight() }

        assertEquals(0, changedSectionId)
        assertTrue((changedPositionMillis ?: 0L) > 30_000L)
    }

    private fun section() = SongSection(
        id = 0,
        startMillis = 0L,
        endMillis = 30_000L,
        label = SectionLabel.Intro,
        prompt = "Escucha el inicio",
    )
}
