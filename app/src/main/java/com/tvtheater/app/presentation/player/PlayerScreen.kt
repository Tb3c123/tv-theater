package com.tvtheater.app.presentation.player

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.tvtheater.app.presentation.theme.IceBluePrimary

@Composable
fun PlayerScreen(
    movieSlug: String,
    movieName: String,
    posterUrl: String?,
    episodeSlug: String,
    episodeName: String,
    embedUrl: String,
    initialPositionMs: Long = 0L,
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val backFocusRequester = remember { FocusRequester() }
    val playPauseFocusRequester = remember { FocusRequester() }

    LaunchedEffect(embedUrl) {
        viewModel.initPlayer(
            movieSlug = movieSlug,
            movieName = movieName,
            posterUrl = posterUrl,
            episodeSlug = episodeSlug,
            episodeName = episodeName,
            embedUrl = embedUrl,
            initialPositionMs = initialPositionMs
        )
    }

    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                            onBack()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            viewModel.togglePlayPause()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            viewModel.setPlaying(true)
                            viewModel.showOsdTemporarily()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            viewModel.setPlaying(false)
                            viewModel.showOsdTemporarily()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (uiState.errorMessage == null) {
                                if (!uiState.isOsdVisible) {
                                    viewModel.showOsdTemporarily()
                                }
                                try {
                                    backFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (uiState.errorMessage == null) {
                                if (!uiState.isOsdVisible) {
                                    viewModel.showOsdTemporarily()
                                }
                                try {
                                    playPauseFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_DPAD_RIGHT,
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER -> {
                            viewModel.showOsdTemporarily()
                            false
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        if (uiState.errorMessage != null) {
            PlayerErrorOverlay(
                errorMessage = uiState.errorMessage!!,
                movieName = uiState.movieName,
                episodeName = uiState.episodeName,
                onBack = onBack,
                onRetry = { viewModel.retry() }
            )
        } else {
            // Video View
            if (uiState.playerMode == PlayerMode.NATIVE_EXOPLAYER && uiState.streamUrl != null) {
                NativePlayerView(
                    streamUrl = uiState.streamUrl!!,
                    isPlaying = uiState.isPlaying,
                    initialPositionMs = uiState.currentPositionMs,
                    onProgressUpdate = { pos, dur ->
                        viewModel.updateProgress(pos, dur)
                    },
                    onError = { error ->
                        viewModel.onPlayerError(error)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (uiState.embedUrl.isNotBlank()) {
                FallbackWebViewPlayer(
                    embedUrl = uiState.embedUrl,
                    isPlaying = uiState.isPlaying,
                    onProgressUpdate = { pos, dur ->
                        viewModel.updateProgress(pos, dur)
                    },
                    onStreamFound = { streamUrl ->
                        viewModel.onDirectStreamFound(streamUrl)
                    },
                    onError = { error ->
                        viewModel.onPlayerError(error)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Buffering indicator
            if (uiState.isBuffering) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IceBluePrimary)
                }
            }

            // TV Remote OSD Overlay
            PlayerOsdOverlay(
                isVisible = uiState.isOsdVisible,
                movieName = uiState.movieName,
                episodeName = uiState.episodeName,
                playerMode = uiState.playerMode,
                isPlaying = uiState.isPlaying,
                currentPositionMs = uiState.currentPositionMs,
                durationMs = uiState.durationMs,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onRewindClick = {
                    val newPos = (uiState.currentPositionMs - 10_000L).coerceAtLeast(0L)
                    viewModel.updateProgress(newPos, uiState.durationMs)
                },
                onForwardClick = {
                    val newPos = (uiState.currentPositionMs + 10_000L).coerceAtMost(uiState.durationMs)
                    viewModel.updateProgress(newPos, uiState.durationMs)
                },
                onTogglePlayerMode = { viewModel.togglePlayerMode() },
                onBack = onBack,
                backFocusRequester = backFocusRequester,
                playPauseFocusRequester = playPauseFocusRequester
            )
        }
    }
}
