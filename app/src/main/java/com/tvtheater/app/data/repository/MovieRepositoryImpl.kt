package com.tvtheater.app.data.repository

import com.tvtheater.app.data.api.NguonCApiService
import com.tvtheater.app.data.api.dto.MovieDetailDto
import com.tvtheater.app.data.api.dto.MovieSummaryDto
import com.tvtheater.app.domain.model.EpisodeItem
import com.tvtheater.app.domain.model.Movie
import com.tvtheater.app.domain.model.MovieDetail
import com.tvtheater.app.domain.model.ServerEpisode
import com.tvtheater.app.domain.repository.MovieRepository

class MovieRepositoryImpl(
    private val apiService: NguonCApiService
) : MovieRepository {

    override suspend fun getLatestMovies(page: Int): Result<List<Movie>> = runCatching {
        val response = apiService.getLatestMovies(page)
        response.items.map { it.toDomain() }
    }

    override suspend fun getMoviesByCategory(category: String, page: Int): Result<List<Movie>> = runCatching {
        val response = apiService.getMoviesByCategory(category, page)
        response.items.map { it.toDomain() }
    }

    override suspend fun getMoviesByGenre(genre: String, page: Int): Result<List<Movie>> = runCatching {
        val response = apiService.getMoviesByGenre(genre, page)
        response.items.map { it.toDomain() }
    }

    override suspend fun getMoviesByCountry(country: String, page: Int): Result<List<Movie>> = runCatching {
        val response = apiService.getMoviesByCountry(country, page)
        response.items.map { it.toDomain() }
    }

    override suspend fun searchMovies(keyword: String, page: Int): Result<List<Movie>> = runCatching {
        val response = apiService.searchMovies(keyword, page)
        response.items.map { it.toDomain() }
    }

    override suspend fun getMovieDetail(slug: String): Result<MovieDetail> = runCatching {
        val response = apiService.getMovieDetail(slug)
        response.movie.toDomain()
    }

    private fun MovieSummaryDto.toDomain(): Movie {
        return Movie(
            slug = slug,
            name = name,
            originalName = originalName,
            thumbUrl = thumbUrl,
            posterUrl = posterUrl,
            quality = quality ?: "HD",
            currentEpisode = currentEpisode,
            year = year,
            description = description
        )
    }

    private fun MovieDetailDto.toDomain(): MovieDetail {
        return MovieDetail(
            slug = slug,
            name = name,
            originalName = originalName,
            thumbUrl = thumbUrl,
            posterUrl = posterUrl,
            quality = quality ?: "HD",
            currentEpisode = currentEpisode,
            totalEpisodes = totalEpisodes,
            time = time,
            director = director,
            casts = casts,
            year = year,
            description = description,
            servers = episodes.map { s ->
                ServerEpisode(
                    serverName = s.serverName,
                    episodes = s.items.map { ep ->
                        EpisodeItem(
                            name = ep.name,
                            slug = ep.slug,
                            embedUrl = ep.embed
                        )
                    }
                )
            }
        )
    }
}
