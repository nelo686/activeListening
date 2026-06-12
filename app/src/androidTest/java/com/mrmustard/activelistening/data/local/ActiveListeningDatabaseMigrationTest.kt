package com.mrmustard.activelistening.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActiveListeningDatabaseMigrationTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private var database: ActiveListeningDatabase? = null

    @Before
    fun setUp() {
        context.deleteDatabase(TEST_DATABASE_NAME)
    }

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(TEST_DATABASE_NAME)
    }

    @Test
    fun migratesFromVersion1To4AndPreservesSettings() {
        createVersion1Database()

        val migratedDatabase = openMigratedDatabase()
        val sqliteDatabase = migratedDatabase.openHelper.writableDatabase

        sqliteDatabase.query("SELECT learning_level, guidance_intensity FROM user_settings").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Advanced", cursor.getString(0))
            assertEquals("Reduced", cursor.getString(1))
        }
        assertTrue(sqliteDatabase.hasTable("song_structure_sections"))
        assertTrue(sqliteDatabase.hasTable("saved_listening_sessions"))
        assertTrue(sqliteDatabase.hasTable("learning_progress_sessions"))
    }

    @Test
    fun migratesFromVersion2To4AndPreservesStructure() {
        createVersion2Database()

        val migratedDatabase = openMigratedDatabase()
        val sqliteDatabase = migratedDatabase.openHelper.writableDatabase

        sqliteDatabase.query(
            "SELECT label, start_millis, end_millis FROM song_structure_sections " +
                "WHERE song_key = 'content://song'",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Verse", cursor.getString(0))
            assertEquals(0L, cursor.getLong(1))
            assertEquals(30_000L, cursor.getLong(2))
        }
        assertTrue(sqliteDatabase.hasTable("saved_listening_sessions"))
        assertTrue(sqliteDatabase.hasColumn("song_structure_sections", "custom_label"))
        assertTrue(sqliteDatabase.hasTable("learning_progress_sessions"))
    }

    @Test
    fun migratesFromVersion3To4AndPreservesSavedSessions() {
        createVersion3Database()

        val sqliteDatabase = openMigratedDatabase().openHelper.writableDatabase
        sqliteDatabase.query("SELECT display_name FROM saved_listening_sessions").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Practice.mp3", cursor.getString(0))
        }
        assertTrue(sqliteDatabase.hasColumn("song_structure_sections", "custom_label"))
        assertTrue(sqliteDatabase.hasTable("learning_progress_sessions"))
    }

    private fun createVersion1Database() {
        openLegacyDatabase().use { database ->
            database.execSQL(CREATE_USER_SETTINGS_TABLE)
            database.execSQL(
                """
                INSERT INTO user_settings (id, learning_level, guidance_intensity)
                VALUES (1, 'Advanced', 'Reduced')
                """.trimIndent(),
            )
            database.version = 1
        }
    }

    private fun createVersion2Database() {
        openLegacyDatabase().use { database ->
            database.execSQL(CREATE_USER_SETTINGS_TABLE)
            database.execSQL(CREATE_SONG_STRUCTURE_TABLE)
            database.execSQL(
                """
                INSERT INTO song_structure_sections (
                    song_key, version, section_id, position, start_millis, end_millis,
                    label, status, prompt, is_approximate,
                    musical_contrast_confidence, musical_contrast_explanation
                ) VALUES (
                    'content://song', 'Edited', 0, 0, 0, 30000,
                    'Verse', 'Suggested', 'Escucha el cambio.', 1, NULL, NULL
                )
                """.trimIndent(),
            )
            database.version = 2
        }
    }

    private fun createVersion3Database() {
        openLegacyDatabase().use { database ->
            database.execSQL(CREATE_USER_SETTINGS_TABLE)
            database.execSQL(CREATE_SONG_STRUCTURE_TABLE)
            database.execSQL(CREATE_SAVED_SESSIONS_TABLE)
            database.execSQL(
                """
                INSERT INTO saved_listening_sessions (
                    song_key, display_name, mime_type, duration_millis,
                    last_position_millis, created_at_millis, updated_at_millis
                ) VALUES ('content://song', 'Practice.mp3', 'audio/mpeg', 120000, 30000, 1, 2)
                """.trimIndent(),
            )
            database.version = 3
        }
    }

    private fun openLegacyDatabase(): SQLiteDatabase =
        context.openOrCreateDatabase(TEST_DATABASE_NAME, Context.MODE_PRIVATE, null)

    private fun openMigratedDatabase(): ActiveListeningDatabase =
        Room.databaseBuilder(
            context,
            ActiveListeningDatabase::class.java,
            TEST_DATABASE_NAME,
        )
            .addMigrations(*DatabaseMigrations.all)
            .allowMainThreadQueries()
            .build()
            .also { database = it }

    private fun androidx.sqlite.db.SupportSQLiteDatabase.hasTable(tableName: String): Boolean =
        query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(tableName),
        ).use { cursor -> cursor.moveToFirst() }

    private fun androidx.sqlite.db.SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean =
        query("PRAGMA table_info($tableName)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            generateSequence { if (cursor.moveToNext()) cursor.getString(nameIndex) else null }
                .any { it == columnName }
        }

    private companion object {
        const val TEST_DATABASE_NAME = "active-listening-migration-test.db"

        val CREATE_USER_SETTINGS_TABLE =
            """
            CREATE TABLE IF NOT EXISTS user_settings (
                id INTEGER NOT NULL PRIMARY KEY,
                learning_level TEXT NOT NULL,
                guidance_intensity TEXT NOT NULL
            )
            """.trimIndent()

        val CREATE_SONG_STRUCTURE_TABLE =
            """
            CREATE TABLE IF NOT EXISTS song_structure_sections (
                song_key TEXT NOT NULL,
                version TEXT NOT NULL,
                section_id INTEGER NOT NULL,
                position INTEGER NOT NULL,
                start_millis INTEGER NOT NULL,
                end_millis INTEGER NOT NULL,
                label TEXT NOT NULL,
                status TEXT NOT NULL,
                prompt TEXT NOT NULL,
                is_approximate INTEGER NOT NULL,
                musical_contrast_confidence TEXT,
                musical_contrast_explanation TEXT,
                PRIMARY KEY(song_key, version, section_id)
            )
            """.trimIndent()

        val CREATE_SAVED_SESSIONS_TABLE =
            """
            CREATE TABLE IF NOT EXISTS saved_listening_sessions (
                song_key TEXT NOT NULL PRIMARY KEY,
                display_name TEXT NOT NULL,
                mime_type TEXT,
                duration_millis INTEGER NOT NULL,
                last_position_millis INTEGER NOT NULL,
                created_at_millis INTEGER NOT NULL,
                updated_at_millis INTEGER NOT NULL
            )
            """.trimIndent()
    }
}
