package com.tvtheater.app.data.api

import com.tvtheater.app.data.api.dto.FilmDetailResponse
import com.tvtheater.app.data.api.dto.FilmListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NguonCApiService {

    @GET("films/phim-moi-cap-nhat")
    suspend fun getLatestMovies(
        @Query("page") page: Int = 1
    ): FilmListResponse

    @GET("films/danh-sach/{category}")
    suspend fun getMoviesByCategory(
        @Path("category") category: String,
        @Query("page") page: Int = 1
    ): FilmListResponse

    @GET("films/the-loai/{genre}")
    suspend fun getMoviesByGenre(
        @Path("genre") genre: String,
        @Query("page") page: Int = 1
    ): FilmListResponse

    @GET("films/quoc-gia/{country}")
    suspend fun getMoviesByCountry(
        @Path("country") country: String,
        @Query("page") page: Int = 1
    ): FilmListResponse

    @GET("films/search")
    suspend fun searchMovies(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1
    ): FilmListResponse

    @GET("film/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String
    ): FilmDetailResponse
}
