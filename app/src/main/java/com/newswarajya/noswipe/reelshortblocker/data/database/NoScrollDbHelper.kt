package com.newswarajya.noswipe.reelshortblocker.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockRecord
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.model.UnlockSession

/**
 * Clean-room SQLite implementation matching the reverse-engineered NoScroll schema:
 * - device_unlock_sessions(sessionId TEXT PRIMARY KEY, unlockedOn INTEGER, lockedOn INTEGER)
 * - shorts_block_history(id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp INTEGER, packageName TEXT,
 *   blockMode TEXT, planWhenBlocked TEXT, platformId TEXT, sessionId TEXT REFERENCES device_unlock_sessions)
 */
class NoScrollDbHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_UNLOCK_SESSIONS (
                sessionId TEXT PRIMARY KEY,
                unlockedOn INTEGER NOT NULL,
                lockedOn INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_BLOCK_HISTORY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp INTEGER NOT NULL,
                packageName TEXT NOT NULL,
                blockMode TEXT NOT NULL,
                planWhenBlocked TEXT NOT NULL,
                platformId TEXT NOT NULL,
                sessionId TEXT REFERENCES $TABLE_UNLOCK_SESSIONS(sessionId) ON DELETE SET NULL
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_block_timestamp ON $TABLE_BLOCK_HISTORY(timestamp)")
        db.execSQL("CREATE INDEX idx_block_platform ON $TABLE_BLOCK_HISTORY(platformId)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BLOCK_HISTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_UNLOCK_SESSIONS")
        onCreate(db)
    }

    fun upsertUnlockSession(sessionId: String, unlockedOn: Long, lockedOn: Long? = null) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("sessionId", sessionId)
            put("unlockedOn", unlockedOn)
            if (lockedOn != null) {
                put("lockedOn", lockedOn)
            }
        }
        db.insertWithOnConflict(
            TABLE_UNLOCK_SESSIONS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun updateSessionLockedOn(sessionId: String, lockedOn: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("lockedOn", lockedOn)
        }
        db.update(
            TABLE_UNLOCK_SESSIONS,
            values,
            "sessionId = ?",
            arrayOf(sessionId)
        )
    }

    fun insertBlockRecord(record: BlockRecord): Long {
        val db = writableDatabase
        // Ensure referenced sessionId exists if non-null
        if (!record.sessionId.isNullOrBlank()) {
            val existing = querySessionExists(db, record.sessionId)
            if (!existing) {
                val sessionValues = ContentValues().apply {
                    put("sessionId", record.sessionId)
                    put("unlockedOn", record.timestamp - 30_000L)
                }
                db.insertWithOnConflict(
                    TABLE_UNLOCK_SESSIONS,
                    null,
                    sessionValues,
                    SQLiteDatabase.CONFLICT_IGNORE
                )
            }
        }

        val values = ContentValues().apply {
            put("timestamp", record.timestamp)
            put("packageName", record.packageName)
            put("blockMode", record.blockMode.name)
            put("planWhenBlocked", record.planWhenBlocked.name)
            put("platformId", record.platformId)
            if (!record.sessionId.isNullOrBlank()) {
                put("sessionId", record.sessionId)
            } else {
                putNull("sessionId")
            }
        }
        return db.insert(TABLE_BLOCK_HISTORY, null, values)
    }

    private fun querySessionExists(db: SQLiteDatabase, sessionId: String): Boolean {
        db.rawQuery(
            "SELECT 1 FROM $TABLE_UNLOCK_SESSIONS WHERE sessionId = ? LIMIT 1",
            arrayOf(sessionId)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun getAllBlockRecords(limit: Int = 500): List<BlockRecord> {
        val list = mutableListOf<BlockRecord>()
        val db = readableDatabase
        db.rawQuery(
            """
            SELECT id, timestamp, packageName, blockMode, planWhenBlocked, platformId, sessionId
            FROM $TABLE_BLOCK_HISTORY
            ORDER BY timestamp DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(limit.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    BlockRecord(
                        id = cursor.getLong(0),
                        timestamp = cursor.getLong(1),
                        packageName = cursor.getString(2),
                        blockMode = BlockMode.fromName(cursor.getString(3)),
                        planWhenBlocked = DetectionPlan.fromName(cursor.getString(4)),
                        platformId = cursor.getString(5),
                        sessionId = if (cursor.isNull(6)) null else cursor.getString(6)
                    )
                )
            }
        }
        return list
    }

    fun getBlockCountSince(startTimestampMillis: Long): Int {
        val db = readableDatabase
        db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_BLOCK_HISTORY WHERE timestamp >= ?",
            arrayOf(startTimestampMillis.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun getTotalBlockCount(): Int {
        val db = readableDatabase
        db.rawQuery("SELECT COUNT(*) FROM $TABLE_BLOCK_HISTORY", null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun getBlockCountsByPlatform(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val db = readableDatabase
        db.rawQuery(
            "SELECT platformId, COUNT(*) as cnt FROM $TABLE_BLOCK_HISTORY GROUP BY platformId ORDER BY cnt DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result[cursor.getString(0)] = cursor.getInt(1)
            }
        }
        return result
    }

    fun getUnlockSessionsSince(startTimestampMillis: Long, limit: Int = 50): List<UnlockSession> {
        val sessions = mutableListOf<UnlockSession>()
        val db = readableDatabase
        db.rawQuery(
            """
            SELECT sessionId, unlockedOn, lockedOn
            FROM $TABLE_UNLOCK_SESSIONS
            WHERE unlockedOn >= ?
            ORDER BY unlockedOn DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(startTimestampMillis.toString(), limit.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                sessions.add(
                    UnlockSession(
                        sessionId = cursor.getString(0),
                        unlockedOn = cursor.getLong(1),
                        lockedOn = if (cursor.isNull(2)) null else cursor.getLong(2)
                    )
                )
            }
        }
        return sessions
    }

    companion object {
        const val DATABASE_NAME = "noscroll_audit.db"
        const val DATABASE_VERSION = 1
        const val TABLE_UNLOCK_SESSIONS = "device_unlock_sessions"
        const val TABLE_BLOCK_HISTORY = "shorts_block_history"
    }
}
