package com.tvtheater.app.presentation.detail

import com.tvtheater.app.data.local.entity.WatchHistoryEntity
import com.tvtheater.app.domain.model.EpisodeItem
import com.tvtheater.app.domain.model.MovieDetail
import com.tvtheater.app.domain.model.ServerEpisode
import com.tvtheater.app.domain.repository.MovieRepository
import com.tvtheater.app.domain.usecase.ManageHistoryUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val movieRepository: MovieRepository = mockk()
    private val manageHistoryUseCase: ManageHistoryUseCase = mockk()
    private lateinit var viewModel: DetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DetailViewModel(movieRepository, manageHistoryUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadMovieLoadsDetailAndResumeHistory() = runTest {
        val detail = MovieDetail(
            slug = "breaking-bad",
            name = "Breaking Bad",
            servers = listOf(
                ServerEpisode("Server 1", listOf(EpisodeItem("Tập 1", "tap-1", "https://embed.com/1"))),
                ServerEpisode("Server 2", listOf(EpisodeItem("Tập 1", "tap-1", "https://embed.com/2")))
            )
        )
        val history = WatchHistoryEntity(
            movieSlug = "breaking-bad",
            movieName = "Breaking Bad",
            posterUrl = null,
            episodeSlug = "tap-1",
            episodeName = "Tập 1",
            serverName = "Server 1",
            positionMs = 45000L,
            durationMs = 3000000L
        )

        coEvery { movieRepository.getMovieDetail("breaking-bad") } returns Result.success(detail)
        coEvery { manageHistoryUseCase.getHistoryForMovie("breaking-bad") } returns history

        viewModel.loadMovie("breaking-bad")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DetailUiState.Success)
        val success = state as DetailUiState.Success
        assertEquals("Breaking Bad", success.movie.name)
        assertEquals("tap-1", success.resumeEpisodeSlug)
        assertEquals(45000L, success.resumePositionMs)
        assertEquals(0, success.selectedServerIndex)

        // Select server 2
        viewModel.selectServer(1)
        val updatedState = viewModel.uiState.value as DetailUiState.Success
        assertEquals(1, updatedState.selectedServerIndex)
    }

    @Test
    fun loadMovieFailsSetsErrorState() = runTest {
        coEvery { movieRepository.getMovieDetail("error-slug") } returns Result.failure(RuntimeException("Not Found"))
        coEvery { manageHistoryUseCase.getHistoryForMovie("error-slug") } returns null

        viewModel.loadMovie("error-slug")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DetailUiState.Error)
    }
}
