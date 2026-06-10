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

    val all = arrayOf(migration1To2, migration2To3)
}
