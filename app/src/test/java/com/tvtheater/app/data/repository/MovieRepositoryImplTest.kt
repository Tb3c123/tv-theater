package com.tvtheater.app.data.repository

import com.tvtheater.app.data.api.NguonCApiService
import com.tvtheater.app.data.api.dto.EpisodeItemDto
import com.tvtheater.app.data.api.dto.FilmDetailResponse
import com.tvtheater.app.data.api.dto.FilmListResponse
import com.tvtheater.app.data.api.dto.MovieDetailDto
import com.tvtheater.app.data.api.dto.MovieSummaryDto
import com.tvtheater.app.data.api.dto.ServerEpisodeDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieRepositoryImplTest {

    private val fakeApiService = object : NguonCApiService {
        override suspend fun getLatestMovies(page: Int): FilmListResponse {
            return FilmListResponse(
                status = "success",
                items = listOf(
                    MovieSummaryDto(name = "Phim Mới", slug = "phim-moi", quality = "HD")
                )
            )
        }

        override suspend fun getMoviesByCategory(category: String, page: Int): FilmListResponse {
            return FilmListResponse(
                status = "success",
                items = listOf(
                    MovieSummaryDto(name = "Phim Bộ", slug = "phim-bo", quality = "FHD")
                )
            )
        }

        override suspend fun getMoviesByGenre(genre: String, page: Int): FilmListResponse {
            return FilmListResponse(
                status = "success",
                items = listOf(
                    MovieSummaryDto(name = "Phim Hành Động", slug = "phim-hanh-dong", quality = "HD")
                )
            )
        }

        override suspend fun getMoviesByCountry(country: String, page: Int): FilmListResponse {
            return FilmListResponse()
        }

        override suspend fun searchMovies(keyword: String, page: Int): FilmListResponse {
            return FilmListResponse(
                status = "success",
                items = listOf(
                    MovieSummaryDto(name = "Kết Quả Tìm", slug = "ket-qua", quality = "HD")
                )
            )
        }

        override suspend fun getMovieDetail(slug: String): FilmDetailResponse {
            return FilmDetailResponse(
                status = "success",
                movie = MovieDetailDto(
                    name = "Chi Tiết Phim",
                    slug = slug,
                    episodes = listOf(
                        ServerEpisodeDto(
                            serverName = "Vietsub #1",
                            items = listOf(
                                EpisodeItemDto(name = "1", slug = "tap-1", embed = "https://embed.streamc.xyz/embed.php?hash=123")
                            )
                        )
                    )
                )
            )
        }
    }

    private val repository = MovieRepositoryImpl(fakeApiService)

    @Test
    fun `test getLatestMovies maps successfully to domain models`() = runTest {
        val result = repository.getLatestMovies(1)
        assertTrue(result.isSuccess)
        val movies = result.getOrNull()!!
        assertEquals(1, movies.size)
        assertEquals("Phim Mới", movies[0].name)
        assertEquals("phim-moi", movies[0].slug)
    }

    @Test
    fun `test getMovieDetail maps servers and episodes correctly`() = runTest {
        val result = repository.getMovieDetail("test-slug")
        assertTrue(result.isSuccess)
        val detail = result.getOrNull()!!
        assertEquals("Chi Tiết Phim", detail.name)
        assertEquals("test-slug", detail.slug)
        assertEquals(1, detail.servers.size)
        assertEquals("Vietsub #1", detail.servers[0].serverName)
        assertEquals(1, detail.servers[0].episodes.size)
        assertEquals("https://embed.streamc.xyz/embed.php?hash=123", detail.servers[0].episodes[0].embedUrl)
    }

    @Test
    fun `test searchMovies returns mapped results`() = runTest {
        val result = repository.searchMovies("test", 1)
        assertTrue(result.isSuccess)
        val movies = result.getOrNull()!!
        assertEquals(1, movies.size)
        assertEquals("Kết Quả Tìm", movies[0].name)
    }
}
