package com.mrmustard.activelistening.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
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
                """.trimIndent(),
            )
        }
    }

    val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
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
                """.trimIndent(),
            )
        }
    }

    val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE song_structure_sections ADD COLUMN custom_label TEXT")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS learning_progress_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    song_key TEXT NOT NULL,
                    started_at_millis INTEGER NOT NULL,
                    updated_at_millis INTEGER NOT NULL,
                    guidance_intensity TEXT NOT NULL,
                    total_sections INTEGER NOT NULL,
                    reviewed_section_ids TEXT NOT NULL,
                    manual_edits INTEGER NOT NULL,
                    repetitions INTEGER NOT NULL,
                    explanations_consulted INTEGER NOT NULL,
                    exports INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

    val migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE saved_listening_sessions ADD COLUMN title TEXT")
            db.execSQL("ALTER TABLE saved_listening_sessions ADD COLUMN artist TEXT")
        }
    }

    val all = arrayOf(migration1To2, migration2To3, migration3To4, migration4To5)
}
