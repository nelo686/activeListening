package com.mrmustard.activelistening.ui.song

import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.progress.AutonomyLevel
import com.mrmustard.activelistening.domain.progress.LearningProgressSummary
import com.mrmustard.activelistening.ui.theme.ActiveListeningTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SavedSessionsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val first = savedSession("one", "One.mp3")
    private val second = savedSession("two", "Two.mp3")

    @Test
    fun swipeRevealsDeleteWithoutDeletingAndDeleteButtonDeletes() {
        var deletedSongKey: String? = null
        setContent(onDelete = { deletedSongKey = it })

        composeRule.onNodeWithTag("saved_session_one").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        assertNull(deletedSongKey)
        composeRule.onNodeWithTag("saved_session_one")
            .assertLeftPositionInRootIsEqualTo((-48).dp)
        composeRule.onNodeWithTag("delete_saved_session_one").performClick()
        assertEquals("one", deletedSongKey)
    }

    @Test
    fun tappingOpenCardClosesItWithoutResuming() {
        var resumedSongKey: String? = null
        setContent(onResume = { resumedSongKey = it.songKey })

        composeRule.onNodeWithTag("saved_session_one").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("saved_session_one").performClick()
        composeRule.waitForIdle()

        assertNull(resumedSongKey)
        composeRule.onNodeWithTag("saved_session_one")
            .assertLeftPositionInRootIsEqualTo(24.dp)
    }

    @Test
    fun openingSecondCardClosesFirstCard() {
        setContent()

        composeRule.onNodeWithTag("saved_session_one").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("saved_session_two").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("saved_session_one")
            .assertLeftPositionInRootIsEqualTo(24.dp)
    }

    @Test
    fun swipingBackClosesCard() {
        setContent()

        composeRule.onNodeWithTag("saved_session_one").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("saved_session_one").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("saved_session_one")
            .assertLeftPositionInRootIsEqualTo(24.dp)
    }

    @Test
    fun savedSessionShowsLearningProgressSummary() {
        setContent(
            progressSummaries = mapOf(
                first.songKey to LearningProgressSummary(
                    songKey = first.songKey,
                    sessionCount = 3,
                    reviewedSections = 2,
                    totalSections = 4,
                    lastPracticeAtMillis = 1_700_000_000_000L,
                    autonomyLevel = AutonomyLevel.Progressing,
                ),
            ),
        )

        composeRule.onAllNodesWithText("3 sesiones · 2/4 secciones revisadas").assertCountEquals(1)
        composeRule.onAllNodesWithText("En progreso").assertCountEquals(1)
    }

    private fun setContent(
        onResume: (SavedListeningSession) -> Unit = {},
        onDelete: (String) -> Unit = {},
        progressSummaries: Map<String, LearningProgressSummary> = emptyMap(),
    ) {
        composeRule.setContent {
            ActiveListeningTheme {
                SongScreen(
                    state = ActiveListeningUiState(
                        savedSessions = listOf(first, second),
                        progressSummaries = progressSummaries,
                    ),
                    onImportClick = {},
                    onSavedSessionClick = onResume,
                    onDeleteSavedSession = onDelete,
                    onUndoSavedSessionDeletion = {},
                    onSavedSessionDeletionMessageShown = {},
                    onBackToStartClick = {},
                    onSettingsClick = {},
                    onPlayClick = {},
                    onPauseClick = {},
                    onSeek = {},
                    onStartGuidedSession = {},
                    onSectionSelected = {},
                    onSectionEditorDismiss = {},
                    onSectionLabelSelected = {},
                    onSectionCustomLabelChanged = {},
                    onSectionStatusClick = {},
                    onSectionMusicalContrastClick = {},
                    onAdjustSectionStart = {},
                    onAdjustSectionEnd = {},
                    onSplitAtCurrentPosition = {},
                    onMergeWithPrevious = {},
                    onMergeWithNext = {},
                    onRepeatSection = {},
                    onConfirmGuidedSection = {},
                    onMarkGuidedSectionUncertain = {},
                    onRepeatGuidedPrompt = {},
                    onSkipGuidedSection = {},
                    onRestoreOriginalProposal = {},
                    onExportMapClick = {},
                    onErrorShown = {},
                    onMapExportMessageShown = {},
                )
            }
        }
    }

    private fun savedSession(songKey: String, displayName: String): SavedListeningSession =
        SavedListeningSession(
            songKey = songKey,
            displayName = displayName,
            mimeType = "audio/mpeg",
            durationMillis = 120_000L,
            lastPositionMillis = 42_000L,
            createdAtMillis = 10L,
            updatedAtMillis = 20L,
        )
}
