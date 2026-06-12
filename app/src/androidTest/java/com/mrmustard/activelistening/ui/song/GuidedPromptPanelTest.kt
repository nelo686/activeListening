package com.mrmustard.activelistening.ui.song

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme
import org.junit.Rule
import org.junit.Test

class GuidedPromptPanelTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val section = SongSection(
        id = 1,
        startMillis = 0L,
        endMillis = 30_000L,
        label = SectionLabel.Verse,
        prompt = "Escucha si cambia la energia.",
    )

    @Test
    fun normalIntensityShowsPromptAndAllActions() {
        setContent(GuidanceIntensity.Normal)

        composeRule.onAllNodesWithText(section.prompt).assertCountEquals(1)
        composeRule.onAllNodesWithText("Marcar dudoso").assertCountEquals(1)
        composeRule.onAllNodesWithText("Volver 8 s").assertCountEquals(1)
    }

    @Test
    fun reducedIntensityHidesPromptAndKeepsEssentialActions() {
        setContent(GuidanceIntensity.Reduced)

        composeRule.onAllNodesWithText(section.prompt).assertCountEquals(0)
        composeRule.onAllNodesWithText("Confirmar cambio").assertCountEquals(1)
        composeRule.onAllNodesWithText("Saltar por ahora").assertCountEquals(1)
        composeRule.onAllNodesWithText("Marcar dudoso").assertCountEquals(0)
    }

    private fun setContent(intensity: GuidanceIntensity) {
        composeRule.setContent {
            ActiveListeningTheme {
                GuidedPromptPanel(
                    section = section,
                    isGuidanceLoading = false,
                    guidanceError = null,
                    guidanceIntensity = intensity,
                    onConfirm = {},
                    onMarkUncertain = {},
                    onRepeat = {},
                    onSkip = {},
                )
            }
        }
    }
}
