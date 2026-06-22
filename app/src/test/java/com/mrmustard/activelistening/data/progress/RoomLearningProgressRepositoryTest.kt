package com.mrmustard.activelistening.data.progress

import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.progress.AutonomyLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomLearningProgressRepositoryTest {
    private val dao = FakeLearningProgressDao()
    private val repository = RoomLearningProgressRepository(dao)

    @Test
    fun `records reviewed sections without duplicates`() = runTest {
        val sessionId = repository.startSession("song", GuidanceIntensity.Normal, 4)

        repository.markSectionReviewed(sessionId, 1)
        repository.markSectionReviewed(sessionId, 1)
        repository.markSectionReviewed(sessionId, 2)

        val summary = repository.summaries.first().getValue("song")
        assertEquals(2, summary.reviewedSections)
        assertEquals(4, summary.totalSections)
        assertEquals(AutonomyLevel.Progressing, summary.autonomyLevel)
    }

    @Test
    fun `recent reduced sessions with little support become more autonomous`() = runTest {
        repeat(2) {
            val id = repository.startSession("song", GuidanceIntensity.Reduced, 4)
            repository.markSectionReviewed(id, it)
        }

        val summary = repository.summaries.first().getValue("song")
        assertEquals(2, summary.sessionCount)
        assertEquals(AutonomyLevel.MoreAutonomous, summary.autonomyLevel)
    }

    @Test
    fun `repetitions and explanations keep progress guided`() = runTest {
        repeat(2) {
            val id = repository.startSession("song", GuidanceIntensity.Reduced, 2)
            repeat(2) {
                repository.recordRepetition(id)
                repository.recordExplanationConsulted(id)
            }
        }

        val summary = repository.summaries.first().getValue("song")
        assertEquals(AutonomyLevel.Progressing, summary.autonomyLevel)
    }

    @Test
    fun `manual edits and exports are included in summary`() = runTest {
        val sessionId = repository.startSession("song", GuidanceIntensity.Normal, 4)

        repeat(3) { repository.recordManualEdit(sessionId) }
        repository.recordExport(sessionId)

        val summary = repository.summaries.first().getValue("song")
        assertEquals(3, summary.manualEdits)
        assertEquals(1, summary.exports)
    }
}

private class FakeLearningProgressDao : LearningProgressDao {
    private val entities = mutableListOf<LearningProgressSessionEntity>()
    private val flow = MutableStateFlow<List<LearningProgressSessionEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<LearningProgressSessionEntity>> = flow
    override suspend fun getForSong(songKey: String) = entities.filter { it.songKey == songKey }
    override suspend fun getById(sessionId: Long) = entities.firstOrNull { it.id == sessionId }

    override suspend fun upsert(entity: LearningProgressSessionEntity): Long {
        val stored = if (entity.id == 0L) entity.copy(id = nextId++) else entity
        entities.removeAll { it.id == stored.id }
        entities += stored
        flow.value = entities.toList()
        return stored.id
    }

    override suspend fun upsertAll(entities: List<LearningProgressSessionEntity>) {
        entities.forEach { upsert(it) }
    }

    override suspend fun deleteForSong(songKey: String) {
        entities.removeAll { it.songKey == songKey }
        flow.value = entities.toList()
    }
}
