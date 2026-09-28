package com.tvtheater.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tvtheater.app.di.AppContainer
import com.tvtheater.app.presentation.detail.DetailScreen
import com.tvtheater.app.presentation.detail.DetailViewModel
import com.tvtheater.app.presentation.home.HomeScreen
import com.tvtheater.app.presentation.home.HomeViewModel
import com.tvtheater.app.presentation.player.PlayerScreen
import com.tvtheater.app.presentation.player.PlayerViewModel
import com.tvtheater.app.presentation.search.SearchScreen
import com.tvtheater.app.presentation.search.SearchViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object NavRoutes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val DETAIL = "detail/{movieSlug}"
    const val PLAYER = "player?movieSlug={movieSlug}&movieName={movieName}&posterUrl={posterUrl}&episodeSlug={episodeSlug}&episodeName={episodeName}&embedUrl={embedUrl}&initialPositionMs={initialPositionMs}"

    fun detail(movieSlug: String): String = "detail/$movieSlug"

    fun player(
        movieSlug: String,
        movieName: String,
        posterUrl: String?,
        episodeSlug: String,
        episodeName: String,
        embedUrl: String,
        initialPositionMs: Long = 0L
    ): String {
        val encodedMovieSlug = URLEncoder.encode(movieSlug, StandardCharsets.UTF_8.toString())
        val encodedMovieName = URLEncoder.encode(movieName, StandardCharsets.UTF_8.toString())
        val encodedPoster = URLEncoder.encode(posterUrl ?: "", StandardCharsets.UTF_8.toString())
        val encodedEpisodeSlug = URLEncoder.encode(episodeSlug, StandardCharsets.UTF_8.toString())
        val encodedEpisodeName = URLEncoder.encode(episodeName, StandardCharsets.UTF_8.toString())
        val encodedEmbedUrl = URLEncoder.encode(embedUrl, StandardCharsets.UTF_8.toString())

        return "player?movieSlug=$encodedMovieSlug&movieName=$encodedMovieName&posterUrl=$encodedPoster&episodeSlug=$encodedEpisodeSlug&episodeName=$encodedEpisodeName&embedUrl=$encodedEmbedUrl&initialPositionMs=$initialPositionMs"
    }
}

@Composable
fun TVNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier
    ) {
        // HOME SCREEN
        composable(NavRoutes.HOME) {
            val homeViewModel = remember {
                HomeViewModel(
                    getRandomCatalogUseCase = appContainer.getRandomCatalogUseCase,
                    manageHistoryUseCase = appContainer.manageHistoryUseCase
                )
            }

            HomeScreen(
                viewModel = homeViewModel,
                onMovieClick = { movie ->
                    navController.navigate(NavRoutes.detail(movie.slug))
                },
                onNavigateSearch = {
                    navController.navigate(NavRoutes.SEARCH)
                }
            )
        }

        // SEARCH SCREEN
        composable(NavRoutes.SEARCH) {
            val searchViewModel = remember {
                SearchViewModel(movieRepository = appContainer.movieRepository)
            }

            SearchScreen(
                viewModel = searchViewModel,
                onMovieClick = { movie ->
                    navController.navigate(NavRoutes.detail(movie.slug))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // DETAIL SCREEN
        composable(
            route = NavRoutes.DETAIL,
            arguments = listOf(
                navArgument("movieSlug") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieSlug = backStackEntry.arguments?.getString("movieSlug") ?: ""
            val detailViewModel = remember {
                DetailViewModel(
                    movieRepository = appContainer.movieRepository,
                    manageHistoryUseCase = appContainer.manageHistoryUseCase
                )
            }

            DetailScreen(
                movieSlug = movieSlug,
                viewModel = detailViewModel,
                onPlayEpisode = { slug, name, poster, epSlug, epName, embed ->
                    navController.navigate(
                        NavRoutes.player(
                            movieSlug = slug,
                            movieName = name,
                            posterUrl = poster,
                            episodeSlug = epSlug,
                            episodeName = epName,
                            embedUrl = embed
                        )
                    )
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // PLAYER SCREEN
        composable(
            route = NavRoutes.PLAYER,
            arguments = listOf(
                navArgument("movieSlug") { type = NavType.StringType; defaultValue = "" },
                navArgument("movieName") { type = NavType.StringType; defaultValue = "" },
                navArgument("posterUrl") { type = NavType.StringType; defaultValue = "" },
                navArgument("episodeSlug") { type = NavType.StringType; defaultValue = "" },
                navArgument("episodeName") { type = NavType.StringType; defaultValue = "" },
                navArgument("embedUrl") { type = NavType.StringType; defaultValue = "" },
                navArgument("initialPositionMs") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val movieSlug = decodeParam(backStackEntry.arguments?.getString("movieSlug"))
            val movieName = decodeParam(backStackEntry.arguments?.getString("movieName"))
            val posterUrl = decodeParam(backStackEntry.arguments?.getString("posterUrl")).takeIf { it.isNotBlank() }
            val episodeSlug = decodeParam(backStackEntry.arguments?.getString("episodeSlug"))
            val episodeName = decodeParam(backStackEntry.arguments?.getString("episodeName"))
            val embedUrl = decodeParam(backStackEntry.arguments?.getString("embedUrl"))
            val initialPositionMs = backStackEntry.arguments?.getLong("initialPositionMs") ?: 0L

            val playerViewModel = remember {
                PlayerViewModel(
                    extractStreamUrlUseCase = appContainer.extractStreamUrlUseCase,
                    manageHistoryUseCase = appContainer.manageHistoryUseCase
                )
            }

            PlayerScreen(
                movieSlug = movieSlug,
                movieName = movieName,
                posterUrl = posterUrl,
                episodeSlug = episodeSlug,
                episodeName = episodeName,
                embedUrl = embedUrl,
                initialPositionMs = initialPositionMs,
                viewModel = playerViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun decodeParam(param: String?): String {
    if (param.isNullOrEmpty()) return ""
    return try {
        URLDecoder.decode(param, StandardCharsets.UTF_8.toString())
    } catch (_: Exception) {
        param
    }
}
