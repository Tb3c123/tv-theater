package com.tvtheater.app.domain.repository

import com.tvtheater.app.domain.model.Movie
import com.tvtheater.app.domain.model.MovieDetail

interface MovieRepository {
    suspend fun getLatestMovies(page: Int = 1): Result<List<Movie>>
    suspend fun getMoviesByCategory(category: String, page: Int = 1): Result<List<Movie>>
    suspend fun getMoviesByGenre(genre: String, page: Int = 1): Result<List<Movie>>
    suspend fun getMoviesByCountry(country: String, page: Int = 1): Result<List<Movie>>
    suspend fun searchMovies(keyword: String, page: Int = 1): Result<List<Movie>>
    suspend fun getMovieDetail(slug: String): Result<MovieDetail>
}
