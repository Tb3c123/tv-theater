package com.tvtheater.app.data.repository

import com.tvtheater.app.data.local.dao.WatchHistoryDao
import com.tvtheater.app.data.local.dao.WatchlistDao
import com.tvtheater.app.data.local.entity.WatchHistoryEntity
import com.tvtheater.app.data.local.entity.WatchlistEntity
import com.tvtheater.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class HistoryRepositoryImpl(
    private val historyDao: WatchHistoryDao,
    private val watchlistDao: WatchlistDao
) : HistoryRepository {

    override fun getWatchHistory(): Flow<List<WatchHistoryEntity>> {
        return historyDao.getAllHistory()
    }

    override suspend fun getHistoryForMovie(slug: String): WatchHistoryEntity? {
        return historyDao.getHistoryBySlug(slug)
    }

    override suspend fun saveProgress(
        movieSlug: String,
        movieName: String,
        posterUrl: String?,
        episodeSlug: String,
        episodeName: String,
        serverName: String,
        positionMs: Long,
        durationMs: Long
    ) {
        val entity = WatchHistoryEntity(
            movieSlug = movieSlug,
            movieName = movieName,
            posterUrl = posterUrl,
            episodeSlug = episodeSlug,
            episodeName = episodeName,
            serverName = serverName,
            positionMs = positionMs,
            durationMs = durationMs,
            lastWatchedAt = System.currentTimeMillis()
        )
        historyDao.saveHistory(entity)
    }

    override suspend fun deleteHistory(movieSlug: String) {
        historyDao.deleteHistory(movieSlug)
    }

    override fun getWatchlist(): Flow<List<WatchlistEntity>> {
        return watchlistDao.getAllWatchlist()
    }

    override fun isInWatchlist(movieSlug: String): Flow<Boolean> {
        return watchlistDao.isInWatchlist(movieSlug)
    }

    override suspend fun addToWatchlist(item: WatchlistEntity) {
        watchlistDao.addToWatchlist(item)
    }

    override suspend fun removeFromWatchlist(movieSlug: String) {
        watchlistDao.removeFromWatchlist(movieSlug)
    }
}
