package com.mrmustard.activelistening.ui.song

import android.net.Uri
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.export.SongMapExport
import com.mrmustard.activelistening.domain.export.SongMapExportRepository
import com.mrmustard.activelistening.domain.export.SongMapExportResult
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.guidance.GuidedListeningMarkerSuggestion
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.importsong.SongImportGateway
import com.mrmustard.activelistening.domain.importsong.SongImportResult
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.playback.AudioPlayer
import com.mrmustard.activelistening.domain.progress.LearningProgressRepository
import com.mrmustard.activelistening.domain.progress.LearningProgressSession
import com.mrmustard.activelistening.domain.progress.LearningProgressSummary
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.session.SavedListeningSessionRepository
import com.mrmustard.activelistening.domain.session.DeletedSavedSong
import com.mrmustard.activelistening.domain.session.SavedSongRepository
import com.mrmustard.activelistening.domain.settings.UserSettings
import com.mrmustard.activelistening.domain.settings.UserSettingsRepository
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionStatus
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import com.mrmustard.activelistening.domain.structure.SongStructureMap
import com.mrmustard.activelistening.domain.structure.SongStructureRepository
import com.mrmustard.activelistening.domain.usecase.GuidedSessionUseCase
import com.mrmustard.activelistening.domain.usecase.ImportSongUseCase
import com.mrmustard.activelistening.domain.usecase.SectionEditingUseCase
import com.mrmustard.activelistening.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ActiveListeningViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var audioPlayer: FakeAudioPlayer
    private lateinit var importGateway: FakeSongImportGateway
    private lateinit var sessionRepository: FakeSavedListeningSessionRepository
    private lateinit var structureRepository: FakeSongStructureRepository
    private lateinit var savedSongRepository: FakeSavedSongRepository
    private lateinit var exportRepository: FakeSongMapExportRepository
    private lateinit var learningProgressRepository: FakeLearningProgressRepository
    private lateinit var guidedListeningRepository: FakeGuidedListeningRepository
    private lateinit var viewModel: ActiveListeningViewModel

    @Before
    fun setUp() {
        audioPlayer = FakeAudioPlayer()
        importGateway = FakeSongImportGateway()
        sessionRepository = FakeSavedListeningSessionRepository()
        structureRepository = FakeSongStructureRepository()
        savedSongRepository = FakeSavedSongRepository(sessionRepository, structureRepository)
        exportRepository = FakeSongMapExportRepository()
        learningProgressRepository = FakeLearningProgressRepository()
        guidedListeningRepository = FakeGuidedListeningRepository()
        viewModel = ActiveListeningViewModel(
            importSongUseCase = ImportSongUseCase(importGateway, audioPlayer),
            audioPlayer = audioPlayer,
            guidedListeningRepository = guidedListeningRepository,
            userSettingsRepository = FakeUserSettingsRepository(),
            songStructureRepository = structureRepository,
            savedListeningSessionRepository = sessionRepository,
            savedSongRepository = savedSongRepository,
            songMapExportRepository = exportRepository,
            guidedSessionUseCase = GuidedSessionUseCase(),
            sectionEditingUseCase = SectionEditingUseCase(),
            learningProgressRepository = learningProgressRepository,
        )
    }

    @Test
    fun `resuming saved session restores structure and playback position`() = runTest {
        val song = testSong()
        val original = SongStructureFactory.createInitialSections(song.durationMillis)
        val edited = original.map { section ->
            if (section.id == 1) section.copy(label = SectionLabel.Bridge) else section
        }
        importGateway.result = SongImportResult.Success(song)
        structureRepository.structures[song.uri.toString()] = SongStructureMap(original, edited)
        val savedSession = testSession(song, lastPositionMillis = 42_000L)
        sessionRepository.storedSessions[song.uri.toString()] = savedSession

        viewModel.resumeSavedSession(savedSession)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(song, state.importedSong)
        assertEquals(edited, state.sections)
        assertTrue(state.isGuidedSessionActive)
        assertEquals(42_000L, audioPlayer.lastSeekPositionMillis)
    }

    @Test
    fun `importing a new song does not create a saved session`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)

        viewModel.importSong(song.uri)
        advanceUntilIdle()

        assertEquals(song, viewModel.uiState.value.importedSong)
        assertFalse(sessionRepository.storedSessions.containsKey(song.uri.toString()))
    }

    @Test
    fun `starting guided listening creates a saved session`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        viewModel.importSong(song.uri)
        advanceUntilIdle()

        viewModel.startGuidedSession()
        advanceUntilIdle()

        assertTrue(sessionRepository.storedSessions.containsKey(song.uri.toString()))
        assertTrue(viewModel.uiState.value.isGuidedSessionActive)
    }

    @Test
    fun `guided actions confirm and mark current section uncertain`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        viewModel.importSong(song.uri)
        advanceUntilIdle()
        viewModel.startGuidedSession()
        advanceUntilIdle()

        viewModel.confirmGuidedSection()
        assertEquals(SectionStatus.Confirmed, viewModel.uiState.value.sections.first().status)

        viewModel.markGuidedSectionUncertain()
        assertEquals(SectionStatus.Uncertain, viewModel.uiState.value.sections.first().status)
    }

    @Test
    fun `guided action waits for progress session initialization`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        learningProgressRepository.startSessionGate = CompletableDeferred()
        viewModel.importSong(song.uri)
        advanceUntilIdle()

        viewModel.startGuidedSession()
        viewModel.confirmGuidedSection()
        runCurrent()

        assertTrue(learningProgressRepository.reviewedSections.isEmpty())
        learningProgressRepository.startSessionGate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(1L to 0), learningProgressRepository.reviewedSections)
    }

    @Test
    fun `late guidance response does not modify the next song`() = runTest {
        val firstSong = testSong()
        val secondSong = firstSong.copy(
            uri = Uri.parse("content://songs/second.mp3"),
            displayName = "Second.mp3",
            title = "Second",
        )
        val firstResponse = CompletableDeferred<GuidedListeningResult>()
        val secondResponse = CompletableDeferred<GuidedListeningResult>()
        guidedListeningRepository.responses += firstResponse
        guidedListeningRepository.responses += secondResponse

        importGateway.result = SongImportResult.Success(firstSong)
        viewModel.importSong(firstSong.uri)
        advanceUntilIdle()
        viewModel.startGuidedSession()
        runCurrent()

        viewModel.returnToStart()
        importGateway.result = SongImportResult.Success(secondSong)
        viewModel.importSong(secondSong.uri)
        runCurrent()
        viewModel.startGuidedSession()
        runCurrent()

        secondResponse.complete(guidanceResult(prompt = "Pista para la segunda canción"))
        runCurrent()
        firstResponse.complete(guidanceResult(prompt = "Pista obsoleta de la primera canción"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(secondSong, state.importedSong)
        assertEquals("Pista para la segunda canción", state.sections.first().prompt)
        assertEquals(
            "Pista para la segunda canción",
            structureRepository.structures[secondSong.uri.toString()]?.editedSections?.first()?.prompt,
        )
    }

    private fun guidanceResult(prompt: String): GuidedListeningResult =
        GuidedListeningResult.Success(
            markers = listOf(
                GuidedListeningMarkerSuggestion(
                    id = 0,
                    title = "Intro",
                    prompt = prompt,
                ),
            ),
        )

    @Test
    fun `guided repeat seeks eight seconds back without crossing song start`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        viewModel.importSong(song.uri)
        advanceUntilIdle()
        audioPlayer.publishPosition(5_000L)
        advanceUntilIdle()

        viewModel.repeatGuidedPrompt()

        assertEquals(0L, audioPlayer.lastSeekPositionMillis)
        assertTrue(audioPlayer.playCalled)
    }

    @Test
    fun `repeating selected section seeks to its start`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        viewModel.importSong(song.uri)
        advanceUntilIdle()
        viewModel.startGuidedSession()
        advanceUntilIdle()
        val section = viewModel.uiState.value.sections[1]
        viewModel.openSectionEditor(section.id)

        viewModel.repeatSelectedSection()

        assertEquals(section.startMillis, audioPlayer.lastSeekPositionMillis)
    }

    @Test
    fun `marking rhythm change updates timeline state and persisted structure`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        viewModel.importSong(song.uri)
        advanceUntilIdle()
        viewModel.startGuidedSession()
        advanceUntilIdle()
        val section = viewModel.uiState.value.sections.first()
        viewModel.openSectionEditor(section.id)

        viewModel.toggleSelectedSectionMusicalContrast()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.sections.first().musicalContrast != null)
        assertTrue(
            structureRepository.structures[song.uri.toString()]
                ?.editedSections
                ?.first()
                ?.musicalContrast != null,
        )
    }

    @Test
    fun `pending time change after dismiss does not reopen section editor`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        viewModel.importSong(song.uri)
        advanceUntilIdle()
        viewModel.startGuidedSession()
        advanceUntilIdle()
        val section = viewModel.uiState.value.sections.first()
        viewModel.openSectionEditor(section.id)

        viewModel.closeSectionEditor()
        viewModel.setSelectedSectionEnd(section.endMillis - 1_000L)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.editingSectionId)
        assertEquals(section.endMillis - 1_000L, viewModel.uiState.value.sections.first().endMillis)
    }

    @Test
    fun `dragging timeline boundary updates both neighboring sections`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        viewModel.importSong(song.uri)
        advanceUntilIdle()
        viewModel.startGuidedSession()
        advanceUntilIdle()
        val leftSection = viewModel.uiState.value.sections.first()

        viewModel.setTimelineBoundaryAfter(leftSection.id, 35_000L)
        advanceUntilIdle()

        val sections = viewModel.uiState.value.sections
        assertEquals(35_000L, sections[0].endMillis)
        assertEquals(35_000L, sections[1].startMillis)
        assertEquals(
            35_000L,
            structureRepository.structures[song.uri.toString()]?.editedSections?.get(1)?.startMillis,
        )
    }

    @Test
    fun `playing after song ended restarts playback from beginning`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        viewModel.importSong(song.uri)
        advanceUntilIdle()
        audioPlayer.publishPosition(song.durationMillis)
        advanceUntilIdle()

        viewModel.play()

        assertEquals(0L, audioPlayer.lastSeekPositionMillis)
        assertTrue(audioPlayer.playCalled)
    }

    @Test
    fun `deleting saved session removes session and structure and publishes message`() = runTest {
        val song = testSong()
        val session = testSession(song, 42_000L)
        val sections = SongStructureFactory.createInitialSections(song.durationMillis)
        sessionRepository.store(session)
        structureRepository.structures[session.songKey] = SongStructureMap(sections, sections)

        viewModel.deleteSavedSession(session.songKey)
        advanceUntilIdle()

        assertFalse(sessionRepository.storedSessions.containsKey(session.songKey))
        assertFalse(structureRepository.structures.containsKey(session.songKey))
        assertEquals(
            session.displayName,
            viewModel.uiState.value.savedSessionDeletionEvent?.deletedDisplayName,
        )
    }

    @Test
    fun `undo saved session deletion restores session and structure`() = runTest {
        val song = testSong()
        val session = testSession(song, 42_000L)
        val sections = SongStructureFactory.createInitialSections(song.durationMillis)
        val structure = SongStructureMap(sections, sections)
        sessionRepository.store(session)
        structureRepository.structures[session.songKey] = structure
        viewModel.deleteSavedSession(session.songKey)
        advanceUntilIdle()

        viewModel.undoSavedSessionDeletion()
        advanceUntilIdle()

        assertEquals(session, sessionRepository.storedSessions[session.songKey])
        assertEquals(structure, structureRepository.structures[session.songKey])
    }

    @Test
    fun `second deletion replaces song available for undo`() = runTest {
        val firstSong = testSong()
        val secondSong = firstSong.copy(
            uri = Uri.parse("content://songs/second.mp3"),
            displayName = "Second.mp3",
        )
        val firstSession = testSession(firstSong, 10_000L)
        val secondSession = testSession(secondSong, 20_000L)
        sessionRepository.store(firstSession)
        sessionRepository.store(secondSession)

        viewModel.deleteSavedSession(firstSession.songKey)
        advanceUntilIdle()
        viewModel.deleteSavedSession(secondSession.songKey)
        advanceUntilIdle()
        viewModel.undoSavedSessionDeletion()
        advanceUntilIdle()

        assertFalse(sessionRepository.storedSessions.containsKey(firstSession.songKey))
        assertEquals(secondSession, sessionRepository.storedSessions[secondSession.songKey])
    }

    @Test
    fun `returning to start pauses playback and saves current position`() = runTest {
        val song = testSong()
        importGateway.result = SongImportResult.Success(song)
        sessionRepository.storedSessions[song.uri.toString()] = testSession(song, 0L)
        viewModel.importSong(song.uri)
        advanceUntilIdle()
        audioPlayer.publishPosition(27_000L)
        advanceUntilIdle()

        viewModel.returnToStart()
        advanceUntilIdle()

        assertTrue(audioPlayer.pauseCalled)
        assertNull(viewModel.uiState.value.importedSong)
        assertEquals(27_000L, sessionRepository.updatedPositions[song.uri.toString()])
    }

    @Test
    fun `exporting without a map reports insufficient structure`() = runTest {
        viewModel.exportMap(Uri.parse("content://export/empty.pdf"))
        advanceUntilIdle()

        assertEquals(MapExportError.InsufficientStructure, viewModel.uiState.value.mapExportError)
        assertFalse(exportRepository.exportCalled)
    }

    @Test
    fun `export write failure is exposed to the UI`() = runTest {
        val song = testSong()
        val sections = SongStructureFactory.createInitialSections(song.durationMillis)
        importGateway.result = SongImportResult.Success(song)
        structureRepository.structures[song.uri.toString()] = SongStructureMap(sections, sections)
        exportRepository.result = SongMapExportResult.UnableToWrite
        viewModel.importSong(song.uri)
        advanceUntilIdle()

        viewModel.exportMap(Uri.parse("content://export/map.pdf"))
        advanceUntilIdle()

        assertTrue(exportRepository.exportCalled)
        assertEquals(MapExportError.UnableToWrite, viewModel.uiState.value.mapExportError)
        assertFalse(viewModel.uiState.value.isExportingMap)
    }

    private fun testSong(): ImportedSong =
        ImportedSong(
            uri = Uri.parse("content://songs/practice.mp3"),
            displayName = "Practice.mp3",
            mimeType = "audio/mpeg",
            durationMillis = 120_000L,
        )

    private fun testSession(
        song: ImportedSong,
        lastPositionMillis: Long,
    ): SavedListeningSession =
        SavedListeningSession(
            songKey = song.uri.toString(),
            displayName = song.displayName,
            title = song.title,
            artist = song.artist,
            mimeType = song.mimeType,
            durationMillis = song.durationMillis,
            lastPositionMillis = lastPositionMillis,
            createdAtMillis = 1L,
            updatedAtMillis = 2L,
        )
}

private class FakeAudioPlayer : AudioPlayer {
    private val state = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = state
    var lastSeekPositionMillis: Long? = null
    var pauseCalled = false
    var playCalled = false

    override fun load(song: ImportedSong) {
        state.value = PlaybackState(
            isReady = true,
            durationMillis = song.durationMillis,
        )
    }

    override fun play() {
        playCalled = true
        state.value = state.value.copy(isPlaying = true)
    }

    override fun pause() {
        pauseCalled = true
        state.value = state.value.copy(isPlaying = false)
    }

    override fun seekTo(positionMillis: Long) {
        lastSeekPositionMillis = positionMillis
        state.value = state.value.copy(positionMillis = positionMillis)
    }

    override fun release() = Unit

    fun publishPosition(positionMillis: Long) {
        state.value = state.value.copy(
            isReady = true,
            positionMillis = positionMillis,
        )
    }
}

private class FakeSongImportGateway : SongImportGateway {
    var result: SongImportResult = SongImportResult.Error(
        com.mrmustard.activelistening.domain.importsong.ImportSongError.UnreadableFile,
    )

    override suspend fun importSong(uri: Uri): SongImportResult = result
}

private class FakeSavedListeningSessionRepository : SavedListeningSessionRepository {
    private val sessionsFlow = MutableStateFlow<List<SavedListeningSession>>(emptyList())
    override val sessions: Flow<List<SavedListeningSession>> = sessionsFlow
    val storedSessions = mutableMapOf<String, SavedListeningSession>()
    val updatedPositions = mutableMapOf<String, Long>()

    override suspend fun getSession(songKey: String): SavedListeningSession? = storedSessions[songKey]

    override suspend fun upsertSession(song: ImportedSong) {
        val existing = storedSessions[song.uri.toString()]
        storedSessions[song.uri.toString()] = testSessionEntity(song, existing?.lastPositionMillis ?: 0L)
        sessionsFlow.value = storedSessions.values.toList()
    }

    override suspend fun restoreSession(session: SavedListeningSession) {
        store(session)
    }

    override suspend fun deleteSession(songKey: String) {
        storedSessions.remove(songKey)
        sessionsFlow.value = storedSessions.values.toList()
    }

    override suspend fun updatePlaybackPosition(songKey: String, positionMillis: Long) {
        updatedPositions[songKey] = positionMillis
        storedSessions[songKey] = storedSessions[songKey]?.copy(lastPositionMillis = positionMillis)
            ?: return
        sessionsFlow.value = storedSessions.values.toList()
    }

    fun store(session: SavedListeningSession) {
        storedSessions[session.songKey] = session
        sessionsFlow.value = storedSessions.values.toList()
    }

    private fun testSessionEntity(song: ImportedSong, positionMillis: Long): SavedListeningSession =
        SavedListeningSession(
            songKey = song.uri.toString(),
            displayName = song.displayName,
            title = song.title,
            artist = song.artist,
            mimeType = song.mimeType,
            durationMillis = song.durationMillis,
            lastPositionMillis = positionMillis,
            createdAtMillis = 1L,
            updatedAtMillis = 2L,
        )
}

private class FakeSongStructureRepository : SongStructureRepository {
    val structures = mutableMapOf<String, SongStructureMap>()

    override suspend fun getStructure(songKey: String): SongStructureMap? = structures[songKey]

    override suspend fun saveStructure(
        songKey: String,
        originalSections: List<SongSection>,
        editedSections: List<SongSection>,
    ) {
        structures[songKey] = SongStructureMap(originalSections, editedSections)
    }

    override suspend fun deleteStructure(songKey: String) {
        structures.remove(songKey)
    }
}

private class FakeSavedSongRepository(
    private val sessionRepository: FakeSavedListeningSessionRepository,
    private val structureRepository: FakeSongStructureRepository,
) : SavedSongRepository {
    override suspend fun deleteSavedSong(songKey: String): DeletedSavedSong? {
        val session = sessionRepository.getSession(songKey) ?: return null
        val deletedSong = DeletedSavedSong(
            session = session,
            structure = structureRepository.getStructure(songKey),
        )
        sessionRepository.deleteSession(songKey)
        structureRepository.deleteStructure(songKey)
        return deletedSong
    }

    override suspend fun restoreSavedSong(deletedSong: DeletedSavedSong) {
        sessionRepository.restoreSession(deletedSong.session)
        deletedSong.structure?.let { structure ->
            structureRepository.saveStructure(
                songKey = deletedSong.session.songKey,
                originalSections = structure.originalSections,
                editedSections = structure.editedSections,
            )
        }
    }
}

private class FakeSongMapExportRepository : SongMapExportRepository {
    var result: SongMapExportResult = SongMapExportResult.Success
    var exportCalled = false

    override suspend fun exportPdf(destination: Uri, map: SongMapExport): SongMapExportResult {
        exportCalled = true
        return result
    }
}

private class FakeUserSettingsRepository : UserSettingsRepository {
    private val settingsFlow = MutableStateFlow(UserSettings())
    override val settings: Flow<UserSettings> = settingsFlow

    override suspend fun updateLearningLevel(level: LearningLevel) {
        settingsFlow.value = settingsFlow.value.copy(learningLevel = level)
    }

    override suspend fun updateGuidanceIntensity(intensity: GuidanceIntensity) {
        settingsFlow.value = settingsFlow.value.copy(guidanceIntensity = intensity)
    }
}

private class FakeGuidedListeningRepository : GuidedListeningRepository {
    val responses = ArrayDeque<CompletableDeferred<GuidedListeningResult>>()

    override suspend fun createGuidedListeningPlan(request: GuidedListeningRequest): GuidedListeningResult {
        val response = responses.removeFirstOrNull() ?: return GuidedListeningResult.UnableToGenerate
        return withContext(NonCancellable) { response.await() }
    }
}

private class FakeLearningProgressRepository : LearningProgressRepository {
    override val summaries: Flow<Map<String, LearningProgressSummary>> = MutableStateFlow(emptyMap())
    private var nextId = 1L
    var startSessionGate: CompletableDeferred<Unit>? = null
    val reviewedSections = mutableListOf<Pair<Long, Int>>()

    override suspend fun startSession(
        songKey: String,
        guidanceIntensity: GuidanceIntensity,
        totalSections: Int,
    ): Long {
        startSessionGate?.await()
        return nextId++
    }

    override suspend fun markSectionReviewed(sessionId: Long, sectionId: Int) {
        reviewedSections += sessionId to sectionId
    }
    override suspend fun recordManualEdit(sessionId: Long) = Unit
    override suspend fun recordRepetition(sessionId: Long) = Unit
    override suspend fun recordExplanationConsulted(sessionId: Long) = Unit
    override suspend fun recordExport(sessionId: Long) = Unit
    override suspend fun getSessions(songKey: String): List<LearningProgressSession> = emptyList()
    override suspend fun replaceSessions(songKey: String, sessions: List<LearningProgressSession>) = Unit
    override suspend fun deleteSessions(songKey: String) = Unit
}
