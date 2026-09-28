package com.tvtheater.app.presentation.player

import com.tvtheater.app.domain.usecase.ExtractStreamUrlUseCase
import com.tvtheater.app.domain.usecase.ManageHistoryUseCase
import com.tvtheater.app.domain.usecase.StreamResult
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val extractStreamUrlUseCase: ExtractStreamUrlUseCase = mockk()
    private val manageHistoryUseCase: ManageHistoryUseCase = mockk(relaxed = true)

    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PlayerViewModel(extractStreamUrlUseCase, manageHistoryUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun directHlsStreamSetsNativePlayerMode() = runTest {
        val embedUrl = "https://embed.streamc.xyz/embed.php?hash=123"
        val m3u8Url = "https://sv1.streamcdn.com/test.m3u8"
        coEvery { extractStreamUrlUseCase(embedUrl) } returns StreamResult.DirectHls(m3u8Url)

        viewModel.initPlayer(
            movieSlug = "phim-test",
            movieName = "Phim Test",
            posterUrl = "https://example.com/poster.jpg",
            episodeSlug = "tap-1",
            episodeName = "Tập 1",
            embedUrl = embedUrl,
            initialPositionMs = 5000L
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PlayerMode.NATIVE_EXOPLAYER, state.playerMode)
        assertEquals(m3u8Url, state.streamUrl)
        assertEquals(5000L, state.currentPositionMs)
    }

    @Test
    fun fallbackEmbedSetsWebViewPlayerMode() = runTest {
        val embedUrl = "https://embed.streamc.xyz/embed.php?hash=456"
        coEvery { extractStreamUrlUseCase(embedUrl) } returns StreamResult.FallbackEmbed(embedUrl)

        viewModel.initPlayer(
            movieSlug = "phim-test",
            movieName = "Phim Test",
            posterUrl = "https://example.com/poster.jpg",
            episodeSlug = "tap-1",
            episodeName = "Tập 1",
            embedUrl = embedUrl,
            initialPositionMs = 0L
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PlayerMode.WEBVIEW_FALLBACK, state.playerMode)
        assertEquals(embedUrl, state.embedUrl)
    }

    @Test
    fun togglePlayPauseInvertsIsPlaying() = runTest {
        assertTrue(viewModel.uiState.value.isPlaying)
        viewModel.togglePlayPause()
        assertFalse(viewModel.uiState.value.isPlaying)
        viewModel.togglePlayPause()
        assertTrue(viewModel.uiState.value.isPlaying)
    }

    @Test
    fun togglePlayerModeSwitchesBetweenNativeAndFallback() = runTest {
        assertEquals(PlayerMode.NATIVE_EXOPLAYER, viewModel.uiState.value.playerMode)
        viewModel.togglePlayerMode()
        assertEquals(PlayerMode.WEBVIEW_FALLBACK, viewModel.uiState.value.playerMode)
        viewModel.togglePlayerMode()
        assertEquals(PlayerMode.NATIVE_EXOPLAYER, viewModel.uiState.value.playerMode)
    }

    @Test
    fun updateProgressSavesHistoryPeriodically() = runTest {
        val embedUrl = "https://example.com/test.m3u8"
        coEvery { extractStreamUrlUseCase(embedUrl) } returns StreamResult.DirectHls(embedUrl)

        viewModel.initPlayer(
            movieSlug = "phim-test",
            movieName = "Phim Test",
            posterUrl = "https://example.com/poster.jpg",
            episodeSlug = "tap-1",
            episodeName = "Tập 1",
            embedUrl = embedUrl,
            initialPositionMs = 0L
        )
        advanceUntilIdle()

        // 1st update at 6000ms (>= 5000ms threshold)
        viewModel.updateProgress(positionMs = 6000L, durationMs = 120000L)
        advanceUntilIdle()

        coVerify(atLeast = 1) {
            manageHistoryUseCase.updateProgress(
                movieSlug = "phim-test",
                movieName = "Phim Test",
                posterUrl = "https://example.com/poster.jpg",
                episodeSlug = "tap-1",
                episodeName = "Tập 1",
                serverName = "Default",
                positionMs = 6000L,
                durationMs = 120000L
            )
        }
    }
}
