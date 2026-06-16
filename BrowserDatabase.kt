package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ad_logs")
data class BlockedAdLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val host: String,
    val pageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface BrowserDao {
    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    suspend fun isBookmarked(url: String): Boolean

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    // History
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 200")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(historyItem: HistoryItem)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryItem(id: Int)

    // Ad Block logs
    @Query("SELECT * FROM ad_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllAdLogs(): Flow<List<BlockedAdLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdLog(adLog: BlockedAdLog)

    @Query("DELETE FROM ad_logs")
    suspend fun clearAllAdLogs()

    @Query("SELECT COUNT(*) FROM ad_logs")
    fun getBlockedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ad_logs")
    suspend fun getBlockedCount(): Int
}

@Database(
    entities = [Bookmark::class, HistoryItem::class, BlockedAdLog::class],
    version = 1,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun browserDao(): BrowserDao
}
