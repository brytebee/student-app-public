package com.atsighi.tutor.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite Database Helper for the Student App's Private Brain.
 * This stores all user-specific data on-device.
 */
class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // User Context: Stores personal info and preferences
        db.execSQL("""
            CREATE TABLE UserContext (
                key TEXT PRIMARY KEY,
                value TEXT,
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent())

        // Learning Gaps: Tracks mistakes and areas for improvement
        db.execSQL("""
            CREATE TABLE LearningGaps (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                language_code TEXT,
                phrase_id TEXT,
                error_count INTEGER DEFAULT 1,
                last_mistake_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent())

        // Scenarios: Local cache of community-verified lessons
        db.execSQL("""
            CREATE TABLE Scenarios (
                scenario_id TEXT PRIMARY KEY,
                category TEXT,
                json_data TEXT,
                is_verified BOOLEAN DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database migrations
    }

    companion object {
        private const val DATABASE_NAME = "NativeTutorPrivate.db"
        private const val DATABASE_VERSION = 1
    }
}
