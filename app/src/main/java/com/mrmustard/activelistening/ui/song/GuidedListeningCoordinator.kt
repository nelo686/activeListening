package com.mrmustard.activelistening.ui.song

import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.progress.LearningProgressRepository
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.usecase.GuidedSessionPlan
import com.mrmustard.activelistening.domain.usecase.GuidedSessionUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@ViewModelScoped
class GuidedListeningCoordinator @Inject constructor(
    private val guidedListeningRepository: GuidedListeningRepository,
    private val guidedSessionUseCase: GuidedSessionUseCase,
    private val learningProgressRepository: LearningProgressRepository,
) {
    val progressSummaries = learningProgressRepository.summaries

    private var progressSessionId: Long? = null
    private var progressInitializationJob: Job? = null
    private var guidanceJob: Job? = null
    private var guidanceRequestId = 0L

    fun createPlan(
        playbackState: PlaybackState,
        songTitle: String?,
        importedSongDurationMillis: Long?,
    ): GuidedSessionPlan = guidedSessionUseCase(
        playbackState = playbackState,
        songTitle = songTitle,
        importedSongDurationMillis = importedSongDurationMillis,
    )

    fun mergeSuggestions(
        sections: List<SongSection>,
        result: GuidedListeningResult.Success,
    ): List<SongSection> = guidedSessionUseCase.mergeSuggestions(sections, result)

    fun startProgressSession(
        scope: CoroutineScope,
        songKey: String,
        guidanceIntensity: GuidanceIntensity,
        totalSections: Int,
    ) {
        cancelProgressSession()
        lateinit var initializationJob: Job
        initializationJob = scope.launch(start = CoroutineStart.LAZY) {
            val sessionId = learningProgressRepository.startSession(
                songKey = songKey,
                guidanceIntensity = guidanceIntensity,
                totalSections = totalSections,
            )
            if (progressInitializationJob === initializationJob) {
                progressSessionId = sessionId
            }
        }
        progressInitializationJob = initializationJob
        initializationJob.start()
    }

    suspend fun restoreProgressSession(
        songKey: String,
        guidanceIntensity: GuidanceIntensity,
        totalSections: Int,
    ) {
        cancelProgressSession()
        progressSessionId = learningProgressRepository.startSession(
            songKey = songKey,
            guidanceIntensity = guidanceIntensity,
            totalSections = totalSections,
        )
    }

    fun requestGuidance(
        scope: CoroutineScope,
        request: GuidedListeningRequest,
        songKey: String,
        isCurrentSong: () -> Boolean,
        onResult: (GuidedListeningResult) -> Unit,
    ) {
        cancelGuidance()
        val requestId = guidanceRequestId
        guidanceJob = scope.launch {
            val result = guidedListeningRepository.createGuidedListeningPlan(request)
            if (requestId == guidanceRequestId && isCurrentSong()) {
                onResult(result)
            }
        }
    }

    fun recordManualEdit(scope: CoroutineScope) = record(scope) {
        learningProgressRepository.recordManualEdit(it)
    }

    fun recordReviewedSection(scope: CoroutineScope, sectionId: Int) = record(scope) {
        learningProgressRepository.markSectionReviewed(it, sectionId)
    }

    fun recordRepetition(scope: CoroutineScope) = record(scope) {
        learningProgressRepository.recordRepetition(it)
    }

    fun recordExplanationConsulted(scope: CoroutineScope) = record(scope) {
        learningProgressRepository.recordExplanationConsulted(it)
    }

    fun recordExport(scope: CoroutineScope) = record(scope) {
        learningProgressRepository.recordExport(it)
    }

    fun cancel() {
        cancelGuidance()
        cancelProgressSession()
    }

    private fun record(scope: CoroutineScope, action: suspend (Long) -> Unit) {
        val currentSessionId = progressSessionId
        if (currentSessionId != null) {
            scope.launch { action(currentSessionId) }
            return
        }
        val initializationJob = progressInitializationJob ?: return
        scope.launch {
            initializationJob.join()
            if (progressInitializationJob !== initializationJob) return@launch
            progressSessionId?.let { action(it) }
        }
    }

    private fun cancelGuidance() {
        guidanceRequestId++
        guidanceJob?.cancel()
        guidanceJob = null
    }

    private fun cancelProgressSession() {
        progressInitializationJob?.cancel()
        progressInitializationJob = null
        progressSessionId = null
    }
}
