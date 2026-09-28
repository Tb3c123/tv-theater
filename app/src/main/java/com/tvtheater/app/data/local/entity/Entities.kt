package com.tvtheater.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watch_history",
    indices = [Index(value = ["last_watched_at"])]
)
data class WatchHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "movie_slug") val movieSlug: String,
    @ColumnInfo(name = "movie_name") val movieName: String,
    @ColumnInfo(name = "poster_url") val posterUrl: String?,
    @ColumnInfo(name = "episode_slug") val episodeSlug: String,
    @ColumnInfo(name = "episode_name") val episodeName: String,
    @ColumnInfo(name = "server_name") val serverName: String = "Default",
    @ColumnInfo(name = "position_ms") val positionMs: Long = 0L,
    @ColumnInfo(name = "duration_ms") val durationMs: Long = 0L,
    @ColumnInfo(name = "last_watched_at") val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "watchlist",
    indices = [Index(value = ["added_at"])]
)
data class WatchlistEntity(
    @PrimaryKey
    @ColumnInfo(name = "movie_slug") val movieSlug: String,
    @ColumnInfo(name = "movie_name") val movieName: String,
    @ColumnInfo(name = "poster_url") val posterUrl: String?,
    @ColumnInfo(name = "quality") val quality: String = "HD",
    @ColumnInfo(name = "current_episode") val currentEpisode: String? = null,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)
