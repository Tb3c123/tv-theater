package com.tvtheater.app.domain.repository

import com.tvtheater.app.data.local.entity.WatchHistoryEntity
import com.tvtheater.app.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>
    suspend fun getHistoryForMovie(slug: String): WatchHistoryEntity?
    suspend fun saveProgress(
        movieSlug: String,
        movieName: String,
        posterUrl: String?,
        episodeSlug: String,
        episodeName: String,
        serverName: String = "Default",
        positionMs: Long,
        durationMs: Long
    )
    suspend fun deleteHistory(movieSlug: String)

    fun getWatchlist(): Flow<List<WatchlistEntity>>
    fun isInWatchlist(movieSlug: String): Flow<Boolean>
    suspend fun addToWatchlist(item: WatchlistEntity)
    suspend fun removeFromWatchlist(movieSlug: String)
}
