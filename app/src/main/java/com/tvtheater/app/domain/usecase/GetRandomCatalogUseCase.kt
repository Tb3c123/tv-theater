package com.tvtheater.app.domain.usecase

import com.tvtheater.app.domain.model.Movie
import com.tvtheater.app.domain.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class MovieSection(
    val title: String,
    val movies: List<Movie>,
    val isGenreSection: Boolean = false
)

data class HomeCatalog(
    val heroMovie: Movie?,
    val sections: List<MovieSection>
)

class GetRandomCatalogUseCase(
    private val repository: MovieRepository
) {
    private val availableGenres = listOf(
        "hanh-dong" to "Hành Động",
        "hoat-hinh" to "Hoạt Hình",
        "vien-tuong" to "Khoa Học Viễn Tưởng",
        "hai-huoc" to "Hài Hước",
        "tinh-cam" to "Tình Cảm",
        "co-trang" to "Cổ Trang",
        "kinh-di" to "Kinh Dị",
        "tam-ly" to "Tâm Lý",
        "hinh-su" to "Hình Sự",
        "gia-dinh" to "Gia Đình"
    )

    suspend operator fun invoke(): Result<HomeCatalog> = runCatching {
        coroutineScope {
            val latestDeferred = async { repository.getLatestMovies(1).getOrDefault(emptyList()) }
            val seriesDeferred = async { repository.getMoviesByCategory("phim-bo", 1).getOrDefault(emptyList()) }
            val singleDeferred = async { repository.getMoviesByCategory("phim-le", 1).getOrDefault(emptyList()) }

            // Pick 2-3 random genres each time
            val randomGenres = availableGenres.shuffled().take(3)
            val genreDeferreds = randomGenres.map { (slug, name) ->
                name to async { repository.getMoviesByGenre(slug, 1).getOrDefault(emptyList()) }
            }

            val latestMovies = latestDeferred.await()
            val seriesMovies = seriesDeferred.await()
            val singleMovies = singleDeferred.await()

            // Pick a random hero from top 5 latest movies
            val heroMovie = if (latestMovies.isNotEmpty()) {
                latestMovies.take(5).random()
            } else null

            val sections = mutableListOf<MovieSection>()

            if (latestMovies.isNotEmpty()) {
                sections.add(MovieSection("Phim Mới Cập Nhật", latestMovies))
            }
            if (seriesMovies.isNotEmpty()) {
                sections.add(MovieSection("Phim Bộ Thịnh Hành", seriesMovies))
            }
            if (singleMovies.isNotEmpty()) {
                sections.add(MovieSection("Phim Lẻ Đặc Sắc", singleMovies))
            }

            for ((genreName, deferred) in genreDeferreds) {
                val movies = deferred.await()
                if (movies.isNotEmpty()) {
                    sections.add(
                        MovieSection(
                            title = "Khám Phá: $genreName",
                            movies = movies,
                            isGenreSection = true
                        )
                    )
                }
            }

            HomeCatalog(heroMovie = heroMovie, sections = sections)
        }
    }
}
