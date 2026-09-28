package com.tvtheater.app.data.repository

import com.tvtheater.app.data.local.dao.WatchHistoryDao
import com.tvtheater.app.data.local.dao.WatchlistDao
import com.tvtheater.app.data.local.entity.WatchHistoryEntity
import com.tvtheater.app.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryRepositoryImplTest {

    private val historyStorage = mutableMapOf<String, WatchHistoryEntity>()
    private val watchlistStorage = mutableMapOf<String, WatchlistEntity>()

    private val fakeHistoryDao = object : WatchHistoryDao {
        override fun getAllHistory(): Flow<List<WatchHistoryEntity>> {
            return flowOf(historyStorage.values.sortedByDescending { it.lastWatchedAt })
        }

        override suspend fun getHistoryBySlug(slug: String): WatchHistoryEntity? {
            return historyStorage[slug]
        }

        override suspend fun saveHistory(history: WatchHistoryEntity) {
            historyStorage[history.movieSlug] = history
        }

        override suspend fun deleteHistory(slug: String) {
            historyStorage.remove(slug)
        }

        override suspend fun clearAllHistory() {
            historyStorage.clear()
        }
    }

    private val fakeWatchlistDao = object : WatchlistDao {
        override fun getAllWatchlist(): Flow<List<WatchlistEntity>> {
            return flowOf(watchlistStorage.values.sortedByDescending { it.addedAt })
        }

        override fun isInWatchlist(slug: String): Flow<Boolean> {
            return flowOf(watchlistStorage.containsKey(slug))
        }

        override suspend fun addToWatchlist(item: WatchlistEntity) {
            watchlistStorage[item.movieSlug] = item
        }

        override suspend fun removeFromWatchlist(slug: String) {
            watchlistStorage.remove(slug)
        }
    }

    private val repository = HistoryRepositoryImpl(fakeHistoryDao, fakeWatchlistDao)

    @Test
    fun `test saving watch progress and retrieving it`() = runTest {
        repository.saveProgress(
            movieSlug = "one-piece",
            movieName = "One Piece",
            posterUrl = "https://img.com/poster.jpg",
            episodeSlug = "tap-10",
            episodeName = "Tập 10",
            positionMs = 120000L,
            durationMs = 1440000L
        )

        val retrieved = repository.getHistoryForMovie("one-piece")
        assertNotNull(retrieved)
        assertEquals("One Piece", retrieved?.movieName)
        assertEquals("tap-10", retrieved?.episodeSlug)
        assertEquals(120000L, retrieved?.positionMs)

        val allHistory = repository.getWatchHistory().first()
        assertEquals(1, allHistory.size)
    }

    @Test
    fun `test adding and removing from watchlist`() = runTest {
        val item = WatchlistEntity(
            movieSlug = "movie-a",
            movieName = "Movie A",
            posterUrl = null
        )

        repository.addToWatchlist(item)
        var inWatchlist = repository.isInWatchlist("movie-a").first()
        assertTrue(inWatchlist)

        repository.removeFromWatchlist("movie-a")
        inWatchlist = repository.isInWatchlist("movie-a").first()
        assertTrue(!inWatchlist)
    }
}
