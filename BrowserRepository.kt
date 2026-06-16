package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val browserDao: BrowserDao) {

    val bookmarks: Flow<List<Bookmark>> = browserDao.getAllBookmarks()
    val history: Flow<List<HistoryItem>> = browserDao.getAllHistory()
    val adLogs: Flow<List<BlockedAdLog>> = browserDao.getAllAdLogs()
    val blockedCount: Flow<Int> = browserDao.getBlockedCountFlow()

    suspend fun addBookmark(url: String, title: String) {
        val cleanTitle = if (title.isBlank()) url else title
        browserDao.insertBookmark(Bookmark(url = url, title = cleanTitle))
    }

    suspend fun removeBookmark(url: String) {
        browserDao.deleteBookmarkByUrl(url)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return browserDao.isBookmarked(url)
    }

    suspend fun addHistory(url: String, title: String) {
        val cleanTitle = if (title.isBlank()) url else title
        browserDao.insertHistory(HistoryItem(url = url, title = cleanTitle))
    }

    suspend fun clearHistory() {
        browserDao.clearAllHistory()
    }

    suspend fun deleteHistory(id: Int) {
        browserDao.deleteHistoryItem(id)
    }

    suspend fun logBlockedAd(host: String, pageUrl: String) {
        browserDao.insertAdLog(BlockedAdLog(host = host, pageUrl = pageUrl))
    }

    suspend fun clearAdLogs() {
        browserDao.clearAllAdLogs()
    }

    suspend fun getBlockedTotalCount(): Int {
        return browserDao.getBlockedCount()
    }
}
