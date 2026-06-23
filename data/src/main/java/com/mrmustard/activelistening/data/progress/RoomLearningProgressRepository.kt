package com.mrmustard.activelistening.data.progress

import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.progress.AutonomyLevel
import com.mrmustard.activelistening.domain.progress.LearningProgressRepository
import com.mrmustard.activelistening.domain.progress.LearningProgressSession
import com.mrmustard.activelistening.domain.progress.LearningProgressSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.enums.enumEntries

class RoomLearningProgressRepository @Inject constructor(
    private val dao: LearningProgressDao,
) : LearningProgressRepository {
    override val summaries: Flow<Map<String, LearningProgressSummary>> =
        dao.observeAll().map { entities ->
            entities.groupBy { it.songKey }.mapValues { (_, sessions) -> sessions.toSummary() }
        }

    override suspend fun startSession(
        songKey: String,
        guidanceIntensity: GuidanceIntensity,
        totalSections: Int,
    ): Long {
        val now = System.currentTimeMillis()
        return dao.upsert(
            LearningProgressSessionEntity(
                songKey = songKey,
                startedAtMillis = now,
                updatedAtMillis = now,
                guidanceIntensity = guidanceIntensity.name,
                totalSections = totalSections,
                reviewedSectionIds = "",
                manualEdits = 0,
                repetitions = 0,
                explanationsConsulted = 0,
                exports = 0,
            ),
        )
    }

    override suspend fun markSectionReviewed(sessionId: Long, sectionId: Int) = update(sessionId) {
        it.copy(reviewedSectionIds = (it.reviewedIds() + sectionId).sorted().joinToString(","))
    }

    override suspend fun recordManualEdit(sessionId: Long) = update(sessionId) {
        it.copy(manualEdits = it.manualEdits + 1)
    }

    override suspend fun recordRepetition(sessionId: Long) = update(sessionId) {
        it.copy(repetitions = it.repetitions + 1)
    }

    override suspend fun recordExplanationConsulted(sessionId: Long) = update(sessionId) {
        it.copy(explanationsConsulted = it.explanationsConsulted + 1)
    }

    override suspend fun recordExport(sessionId: Long) = update(sessionId) {
        it.copy(exports = it.exports + 1)
    }

    override suspend fun getSessions(songKey: String): List<LearningProgressSession> =
        dao.getForSong(songKey).map { it.toDomain() }

    override suspend fun replaceSessions(songKey: String, sessions: List<LearningProgressSession>) {
        dao.deleteForSong(songKey)
        dao.upsertAll(sessions.map { it.toEntity() })
    }

    override suspend fun deleteSessions(songKey: String) = dao.deleteForSong(songKey)

    private suspend fun update(sessionId: Long, transform: (LearningProgressSessionEntity) -> LearningProgressSessionEntity) {
        val current = dao.getById(sessionId) ?: return
        dao.upsert(transform(current).copy(updatedAtMillis = System.currentTimeMillis()))
    }

    private fun List<LearningProgressSessionEntity>.toSummary(): LearningProgressSummary {
        val reviewed = flatMap { it.reviewedIds() }.toSet().size
        val total = maxOf(1, maxOfOrNull { it.totalSections } ?: 1)
        val recent = sortedByDescending { it.startedAtMillis }.take(3)
        val reducedRatio = recent.count { it.guidanceIntensity == GuidanceIntensity.Reduced.name }.toFloat() / recent.size
        val supportPerSection = recent.sumOf { it.repetitions + it.explanationsConsulted }.toFloat() /
            recent.sumOf { it.totalSections.coerceAtLeast(1) }
        val autonomy = when {
            recent.size >= 2 && reviewed > 0 && reducedRatio >= 0.5f && supportPerSection <= 0.5f ->
                AutonomyLevel.MoreAutonomous
            reviewed > 0 || recent.size >= 2 -> AutonomyLevel.Progressing
            else -> AutonomyLevel.Guided
        }
        return LearningProgressSummary(
            sessionCount = size,
            reviewedSections = reviewed.coerceAtMost(total),
            totalSections = total,
            manualEdits = sumOf { it.manualEdits },
            exports = sumOf { it.exports },
            autonomyLevel = autonomy,
        )
    }

    private fun LearningProgressSessionEntity.reviewedIds(): Set<Int> =
        reviewedSectionIds.split(',').mapNotNull { it.toIntOrNull() }.toSet()

    private fun LearningProgressSessionEntity.toDomain() = LearningProgressSession(
        id, songKey, startedAtMillis, updatedAtMillis,
        enumEntries<GuidanceIntensity>().firstOrNull { it.name == guidanceIntensity } ?: GuidanceIntensity.Normal,
        totalSections, reviewedIds(), manualEdits, repetitions, explanationsConsulted, exports,
    )

    private fun LearningProgressSession.toEntity() = LearningProgressSessionEntity(
        id, songKey, startedAtMillis, updatedAtMillis, guidanceIntensity.name, totalSections,
        reviewedSectionIds.sorted().joinToString(","), manualEdits, repetitions, explanationsConsulted, exports,
    )
}
