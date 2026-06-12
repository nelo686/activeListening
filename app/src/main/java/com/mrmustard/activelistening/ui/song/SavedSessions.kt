package com.mrmustard.activelistening.ui.song

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.progress.LearningProgressSummary
import com.mrmustard.activelistening.domain.progress.AutonomyLevel
import java.text.DateFormat
import java.util.Date
import com.mrmustard.activelistening.domain.time.formatTimeCode
import kotlinx.coroutines.launch

fun LazyListScope.savedSessions(
    sessions: List<SavedListeningSession>,
    progressSummaries: Map<String, LearningProgressSummary>,
    onSessionClick: (SavedListeningSession) -> Unit,
    onDeleteSession: (String) -> Unit,
    openSessionKey: String?,
    onOpenSessionChange: (String?) -> Unit,
) {
    if (sessions.isEmpty()) return

    item(key = "saved_sessions_header") {
        SavedSessionsHeader()
    }
    items(
        items = sessions,
        key = SavedListeningSession::songKey,
        contentType = { "saved_session" },
    ) { session ->
        SavedSessionItem(
            session = session,
            progress = progressSummaries[session.songKey],
            isOpen = openSessionKey == session.songKey,
            onOpen = { onOpenSessionChange(session.songKey) },
            onClose = { onOpenSessionChange(null) },
            onClick = {
                if (openSessionKey == session.songKey) {
                    onOpenSessionChange(null)
                } else {
                    onSessionClick(session)
                }
            },
            onDelete = {
                onOpenSessionChange(null)
                onDeleteSession(session.songKey)
            },
        )
    }
}

@Composable
private fun SavedSessionsHeader() {
    Text(
        text = stringResource(R.string.saved_sessions_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun SavedSessionItem(
    session: SavedListeningSession,
    progress: LearningProgressSummary?,
    isOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionWidth = 72.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }
    val layoutDirection = LocalLayoutDirection.current
    val openOffset = if (layoutDirection == LayoutDirection.Ltr) {
        -actionWidthPx
    } else {
        actionWidthPx
    }
    var offsetX by remember(session.songKey) { mutableFloatStateOf(0f) }
    val animationScope = rememberCoroutineScope()
    val shape = RoundedCornerShape(12.dp)

    LaunchedEffect(isOpen, openOffset) {
        animate(
            initialValue = offsetX,
            targetValue = if (isOpen) openOffset else 0f,
        ) { value, _ -> offsetX = value }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .pointerInput(actionWidthPx, layoutDirection, session.songKey, isOpen) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var overSlop = 0f
                    val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, amount ->
                        overSlop = amount
                        change.consume()
                    }
                    if (drag == null) return@awaitEachGesture

                    val minimumOffset = minOf(0f, openOffset)
                    val maximumOffset = maxOf(0f, openOffset)
                    offsetX = (offsetX + overSlop).coerceIn(minimumOffset, maximumOffset)
                    horizontalDrag(drag.id) { change ->
                        val delta = change.positionChange().x
                        offsetX = (offsetX + delta).coerceIn(minimumOffset, maximumOffset)
                        if (delta != 0f) change.consume()
                    }

                    val shouldOpen = kotlin.math.abs(offsetX) >= actionWidthPx / 2f
                    val targetOffset = if (shouldOpen) openOffset else 0f
                    if (shouldOpen == isOpen) {
                        animationScope.launch {
                            animate(offsetX, targetOffset) { value, _ -> offsetX = value }
                        }
                    }
                    if (shouldOpen) onOpen() else onClose()
                }
            },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(actionWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_saved_session_${session.songKey}"),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_24),
                        contentDescription = stringResource(R.string.saved_sessions_delete),
                        tint = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.toInt(), 0) }
                .clickable(onClick = onClick)
                .testTag("saved_session_${session.songKey}"),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = session.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                    )
                    progress?.let {
                        Text(
                            text = stringResource(
                                R.string.saved_sessions_progress,
                                it.sessionCount,
                                it.reviewedSections,
                                it.totalSections,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(
                                R.string.saved_sessions_last_practice,
                                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it.lastPracticeAtMillis)),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = it.autonomyLevel.toDisplayName(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.saved_sessions_resume_context,
                            formatTimeCode(session.lastPositionMillis),
                            formatTimeCode(session.durationMillis),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right_24),
                        contentDescription = stringResource(R.string.saved_sessions_resume),
                    )
                }
            }
        }
    }
}

@Composable
private fun AutonomyLevel.toDisplayName(): String = when (this) {
    AutonomyLevel.Guided -> stringResource(R.string.autonomy_guided)
    AutonomyLevel.Progressing -> stringResource(R.string.autonomy_progressing)
    AutonomyLevel.MoreAutonomous -> stringResource(R.string.autonomy_more_autonomous)
}
