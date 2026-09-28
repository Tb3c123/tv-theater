package com.tvtheater.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tvtheater.app.data.local.entity.WatchHistoryEntity
import com.tvtheater.app.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY last_watched_at DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE movie_slug = :slug LIMIT 1")
    suspend fun getHistoryBySlug(slug: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE movie_slug = :slug")
    suspend fun deleteHistory(slug: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY added_at DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE movie_slug = :slug)")
    fun isInWatchlist(slug: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movie_slug = :slug")
    suspend fun removeFromWatchlist(slug: String)
}
