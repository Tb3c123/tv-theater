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
    fun directHlsStreamSetsStreamUrlWhileKeepingWebViewPlayerMode() = runTest {
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
        assertEquals(PlayerMode.WEBVIEW_FALLBACK, state.playerMode)
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
        assertEquals(PlayerMode.WEBVIEW_FALLBACK, viewModel.uiState.value.playerMode)
        viewModel.togglePlayerMode()
        assertEquals(PlayerMode.NATIVE_EXOPLAYER, viewModel.uiState.value.playerMode)
        viewModel.togglePlayerMode()
        assertEquals(PlayerMode.WEBVIEW_FALLBACK, viewModel.uiState.value.playerMode)
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

    @Test
    fun onDirectStreamFoundUpdatesStreamUrlWhileKeepingWebViewPlayerMode() = runTest {
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

        assertEquals(PlayerMode.WEBVIEW_FALLBACK, viewModel.uiState.value.playerMode)

        val interceptedHlsUrl = "https://cdn.streamc.xyz/hls/master.m3u8"
        viewModel.onDirectStreamFound(interceptedHlsUrl)

        assertEquals(PlayerMode.WEBVIEW_FALLBACK, viewModel.uiState.value.playerMode)
        assertEquals(interceptedHlsUrl, viewModel.uiState.value.streamUrl)
        assertFalse(viewModel.uiState.value.isBuffering)
    }

    @Test
    fun setAdSkippableUpdatesState() = runTest {
        assertFalse(viewModel.uiState.value.isAdSkippable)
        viewModel.setAdSkippable(true)
        assertTrue(viewModel.uiState.value.isAdSkippable)

        viewModel.setAdSkippable(false)
        assertFalse(viewModel.uiState.value.isAdSkippable)
    }

    @Test
    fun skipAdIncrementsTriggerAndClearsSkippable() = runTest {
        viewModel.setAdSkippable(true)
        assertEquals(0, viewModel.uiState.value.skipAdTrigger)
        assertTrue(viewModel.uiState.value.isAdSkippable)

        viewModel.skipAd()
        assertEquals(1, viewModel.uiState.value.skipAdTrigger)
        assertFalse(viewModel.uiState.value.isAdSkippable)
    }

    @Test
    fun resumePopupLifecycleAndActions() = runTest {
        viewModel.onResumePopupDetected("1 phút 40 giây")
        assertEquals("1 phút 40 giây", viewModel.uiState.value.resumePopupTimeText)

        viewModel.confirmResume()
        assertEquals(null, viewModel.uiState.value.resumePopupTimeText)
        assertEquals(1, viewModel.uiState.value.resumeActionTrigger)

        viewModel.onResumePopupDetected("2 phút")
        assertEquals("2 phút", viewModel.uiState.value.resumePopupTimeText)

        viewModel.confirmRestart()
        assertEquals(null, viewModel.uiState.value.resumePopupTimeText)
        assertEquals(2, viewModel.uiState.value.resumeActionTrigger)

        viewModel.onResumePopupDetected("3 phút")
        assertEquals("3 phút", viewModel.uiState.value.resumePopupTimeText)
        viewModel.dismissResumePopup()
        assertEquals(null, viewModel.uiState.value.resumePopupTimeText)
    }

    @Test
    fun universalWebDialogLifecycleAndActions() = runTest {
        val buttonsJson = """[{"index":0,"text":"Tải lại trang","isPrimary":false},{"index":1,"text":"Đóng thông báo","isPrimary":true}]"""
        viewModel.onDialogDetected(
            id = "dlg_123",
            title = "Thông báo phát video",
            message = "Chưa tải hoặc kiểm tra được quảng cáo. Có thể do bộ chặn hoặc kết nối.",
            buttonsJson = buttonsJson
        )

        val active = viewModel.uiState.value.activeDialog
        org.junit.Assert.assertNotNull(active)
        assertEquals("dlg_123", active?.id)
        assertEquals("Thông báo phát video", active?.title)
        assertEquals("Chưa tải hoặc kiểm tra được quảng cáo. Có thể do bộ chặn hoặc kết nối.", active?.message)
        assertEquals(2, active?.buttons?.size)
        assertEquals("Tải lại trang", active?.buttons?.get(0)?.text)
        assertFalse(active?.buttons?.get(0)?.isPrimary ?: true)
        assertEquals("Đóng thông báo", active?.buttons?.get(1)?.text)
        assertTrue(active?.buttons?.get(1)?.isPrimary ?: false)

        // Click action
        viewModel.clickDialogButton(1)
        assertEquals(null, viewModel.uiState.value.activeDialog)
        assertEquals(1, viewModel.uiState.value.dialogActionTrigger)

        // Re-detect & dismiss
        viewModel.onDialogDetected("dlg_456", "Cảnh báo", "Nội dung", buttonsJson)
        org.junit.Assert.assertNotNull(viewModel.uiState.value.activeDialog)
        viewModel.dismissActiveDialog()
        assertEquals(null, viewModel.uiState.value.activeDialog)
        assertEquals(1, viewModel.uiState.value.dialogDismissTrigger)
    }

    @Test
    fun seekDeltaAndSeekToUpdatePositionAndTrigger() = runTest {
        viewModel.updateProgress(positionMs = 30000L, durationMs = 120000L)
        assertEquals(30000L, viewModel.uiState.value.currentPositionMs)
        assertEquals(0, viewModel.uiState.value.seekTrigger)

        // Forward +10s
        viewModel.seekDelta(10_000L)
        assertEquals(40000L, viewModel.uiState.value.currentPositionMs)
        assertEquals(40000L, viewModel.uiState.value.seekTargetMs)
        assertEquals(1, viewModel.uiState.value.seekTrigger)

        // Rewind -10s
        viewModel.seekDelta(-10_000L)
        assertEquals(30000L, viewModel.uiState.value.currentPositionMs)
        assertEquals(30000L, viewModel.uiState.value.seekTargetMs)
        assertEquals(2, viewModel.uiState.value.seekTrigger)

        // Rewind beyond 0 clamps to 0
        viewModel.seekDelta(-50_000L)
        assertEquals(0L, viewModel.uiState.value.currentPositionMs)
        assertEquals(0L, viewModel.uiState.value.seekTargetMs)
        assertEquals(3, viewModel.uiState.value.seekTrigger)

        // Replay to 0:00
        viewModel.seekTo(0L)
        assertEquals(0L, viewModel.uiState.value.currentPositionMs)
        assertEquals(0L, viewModel.uiState.value.seekTargetMs)
        assertEquals(4, viewModel.uiState.value.seekTrigger)

        // Seek forward beyond duration clamps to duration
        viewModel.seekTo(150000L)
        assertEquals(120000L, viewModel.uiState.value.currentPositionMs)
        assertEquals(120000L, viewModel.uiState.value.seekTargetMs)
        assertEquals(5, viewModel.uiState.value.seekTrigger)
    }
}

