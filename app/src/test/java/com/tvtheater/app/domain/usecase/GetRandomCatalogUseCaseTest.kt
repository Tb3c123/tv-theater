package com.tvtheater.app.domain.usecase

import com.tvtheater.app.domain.model.Movie
import com.tvtheater.app.domain.model.MovieDetail
import com.tvtheater.app.domain.repository.MovieRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRandomCatalogUseCaseTest {

    private val fakeMovies = (1..10).map { i ->
        Movie(
            slug = "movie-$i",
            name = "Phim $i",
            quality = "HD",
            thumbUrl = "https://img.com/thumb$i.jpg"
        )
    }

    private val fakeRepository = object : MovieRepository {
        override suspend fun getLatestMovies(page: Int): Result<List<Movie>> {
            return Result.success(fakeMovies)
        }

        override suspend fun getMoviesByCategory(category: String, page: Int): Result<List<Movie>> {
            return Result.success(fakeMovies.take(5))
        }

        override suspend fun getMoviesByGenre(genre: String, page: Int): Result<List<Movie>> {
            return Result.success(fakeMovies.take(4))
        }

        override suspend fun getMoviesByCountry(country: String, page: Int): Result<List<Movie>> {
            return Result.success(emptyList())
        }

        override suspend fun searchMovies(keyword: String, page: Int): Result<List<Movie>> {
            return Result.success(emptyList())
        }

        override suspend fun getMovieDetail(slug: String): Result<MovieDetail> {
            error("Not needed for catalog test")
        }
    }

    private val useCase = GetRandomCatalogUseCase(fakeRepository)

    @Test
    fun `test catalog generates valid hero and multiple sections`() = runTest {
        val result = useCase()
        assertTrue(result.isSuccess)

        val catalog = result.getOrNull()!!
        assertNotNull(catalog.heroMovie)
        assertTrue(fakeMovies.contains(catalog.heroMovie))

        // Check sections count: Latest, Series, Single + random genres
        assertTrue(catalog.sections.isNotEmpty())
        assertTrue(catalog.sections.any { it.title.contains("Mới Cập Nhật") })
        assertTrue(catalog.sections.any { it.title.contains("Phim Bộ") })
    }

    @Test
    fun `test random discovery produces genre sections`() = runTest {
        val result = useCase()
        val catalog = result.getOrNull()!!

        val genreSections = catalog.sections.filter { it.isGenreSection }
        assertTrue("Should contain random genre sections", genreSections.isNotEmpty())
    }
}
