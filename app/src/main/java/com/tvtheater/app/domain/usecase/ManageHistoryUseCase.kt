package com.tvtheater.app.domain.usecase

import com.tvtheater.app.data.local.entity.WatchHistoryEntity
import com.tvtheater.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class ManageHistoryUseCase(
    private val repository: HistoryRepository
) {
    fun getHistoryList(): Flow<List<WatchHistoryEntity>> {
        return repository.getWatchHistory()
    }

    suspend fun getHistoryForMovie(slug: String): WatchHistoryEntity? {
        return repository.getHistoryForMovie(slug)
    }

    suspend fun updateProgress(
        movieSlug: String,
        movieName: String,
        posterUrl: String?,
        episodeSlug: String,
        episodeName: String,
        serverName: String = "Default",
        positionMs: Long,
        durationMs: Long
    ) {
        repository.saveProgress(
            movieSlug = movieSlug,
            movieName = movieName,
            posterUrl = posterUrl,
            episodeSlug = episodeSlug,
            episodeName = episodeName,
            serverName = serverName,
            positionMs = positionMs,
            durationMs = durationMs
        )
    }

    suspend fun removeHistory(movieSlug: String) {
        repository.deleteHistory(movieSlug)
    }
}
