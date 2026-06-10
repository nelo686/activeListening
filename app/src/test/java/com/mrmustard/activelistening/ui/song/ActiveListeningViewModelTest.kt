package com.mrmustard.activelistening.ui.song

import android.net.Uri
import com.mrmustard.activelistening.domain.PlaybackState
import com.mrmustard.activelistening.domain.export.SongMapExport
import com.mrmustard.activelistening.domain.export.SongMapExportRepository
import com.mrmustard.activelistening.domain.export.SongMapExportResult
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.importsong.ImportedSong
import com.mrmustard.activelistening.domain.importsong.SongImportGateway
import com.mrmustard.activelistening.domain.importsong.SongImportResult
import com.mrmustard.activelistening.domain.learning.GuidanceIntensity
import com.mrmustard.activelistening.domain.learning.LearningLevel
import com.mrmustard.activelistening.domain.playback.AudioPlayer
import com.mrmustard.activelistening.domain.session.SavedListeningSession
import com.mrmustard.activelistening.domain.session.SavedListeningSessionRepository
import com.mrmustard.activelistening.domain.settings.UserSettings
import com.mrmustard.activelistening.domain.settings.UserSettingsRepository
import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SongSection
import com.mrmustard.activelistening.domain.structure.SongStructureFactory
import com.mrmustard.activelistening.domain.structure.SongStructureMap
import com.mrmustard.activelistening.domain.structure.SongStructureRepository
import com.mrmustard.activelistening.domain.usecase.GuidedSessionUseCase
import com.mrmustard.activelistening.domain.usecase.ImportSongUseCase
import com.mrmustard.activelistening.domain.usecase.SectionEditingUseCase
import com.mrmustard.activelistening.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
    private lateinit var exportRepository: FakeSongMapExportRepository
    private lateinit var viewModel: ActiveListeningViewModel

    @Before
    fun setUp() {
        audioPlayer = FakeAudioPlayer()
        importGateway = FakeSongImportGateway()
        sessionRepository = FakeSavedListeningSessionRepository()
        structureRepository = FakeSongStructureRepository()
        exportRepository = FakeSongMapExportRepository()
        viewModel = ActiveListeningViewModel(
            importSongUseCase = ImportSongUseCase(importGateway, audioPlayer),
            audioPlayer = audioPlayer,
            guidedListeningRepository = FakeGuidedListeningRepository,
            userSettingsRepository = FakeUserSettingsRepository(),
            songStructureRepository = structureRepository,
            savedListeningSessionRepository = sessionRepository,
            songMapExportRepository = exportRepository,
            guidedSessionUseCase = GuidedSessionUseCase(),
            sectionEditingUseCase = SectionEditingUseCase(),
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

    override fun load(song: ImportedSong) {
        state.value = PlaybackState(
            isReady = true,
            durationMillis = song.durationMillis,
        )
    }

    override fun play() = Unit

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

    override suspend fun updatePlaybackPosition(songKey: String, positionMillis: Long) {
        updatedPositions[songKey] = positionMillis
        storedSessions[songKey] = storedSessions[songKey]?.copy(lastPositionMillis = positionMillis)
            ?: return
        sessionsFlow.value = storedSessions.values.toList()
    }

    private fun testSessionEntity(song: ImportedSong, positionMillis: Long): SavedListeningSession =
        SavedListeningSession(
            songKey = song.uri.toString(),
            displayName = song.displayName,
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

private object FakeGuidedListeningRepository : GuidedListeningRepository {
    override suspend fun createGuidedListeningPlan(request: GuidedListeningRequest): GuidedListeningResult =
        GuidedListeningResult.UnableToGenerate
}
