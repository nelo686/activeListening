package com.mrmustard.activelistening.domain.progress

import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import kotlinx.coroutines.flow.Flow

enum class AutonomyLevel {
    Guided,
    Progressing,
    MoreAutonomous,
}

data class LearningProgressSession(
    val id: Long,
    val songKey: String,
    val startedAtMillis: Long,
    val updatedAtMillis: Long,
    val guidanceIntensity: GuidanceIntensity,
    val totalSections: Int,
    val reviewedSectionIds: Set<Int> = emptySet(),
    val manualEdits: Int = 0,
    val repetitions: Int = 0,
    val explanationsConsulted: Int = 0,
    val exports: Int = 0,
)

data class LearningProgressSummary(
    val sessionCount: Int,
    val reviewedSections: Int,
    val totalSections: Int,
    val manualEdits: Int,
    val exports: Int,
    val autonomyLevel: AutonomyLevel,
)

interface LearningProgressRepository {
    val summaries: Flow<Map<String, LearningProgressSummary>>

    suspend fun startSession(
        songKey: String,
        guidanceIntensity: GuidanceIntensity,
        totalSections: Int,
    ): Long

    suspend fun markSectionReviewed(sessionId: Long, sectionId: Int)
    suspend fun recordManualEdit(sessionId: Long)
    suspend fun recordRepetition(sessionId: Long)
    suspend fun recordExplanationConsulted(sessionId: Long)
    suspend fun recordExport(sessionId: Long)
    suspend fun getSessions(songKey: String): List<LearningProgressSession>
    suspend fun replaceSessions(songKey: String, sessions: List<LearningProgressSession>)
    suspend fun deleteSessions(songKey: String)
}
