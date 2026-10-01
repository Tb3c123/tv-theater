package com.tvtheater.app.presentation.player

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.tvtheater.app.presentation.theme.DarkNavySurface
import com.tvtheater.app.presentation.theme.DeepNavyBackground
import com.tvtheater.app.presentation.theme.IceBluePrimary
import com.tvtheater.app.presentation.theme.TextSoftWhite
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
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
    val isAdActive = uiState.playerMode == PlayerMode.WEBVIEW_FALLBACK && uiState.isAdSkippable
    val isResumeDialogVisible = uiState.resumePopupTimeText != null
    val isGenericDialogVisible = uiState.activeDialog != null
    val isDialogOpen = isGenericDialogVisible || isResumeDialogVisible
    var isSkipFocused by remember { mutableStateOf(false) }
    var isBackFocused by remember { mutableStateOf(false) }
    var isResumeFocused by remember { mutableStateOf(false) }
    var isRestartFocused by remember { mutableStateOf(false) }
    val rootFocusRequester = remember { FocusRequester() }
    val adBackFocusRequester = remember { FocusRequester() }
    val adSkipFocusRequester = remember { FocusRequester() }
    val osdBackFocusRequester = remember { FocusRequester() }
    val playPauseFocusRequester = remember { FocusRequester() }
    val osdRewindFocusRequester = remember { FocusRequester() }
    val osdForwardFocusRequester = remember { FocusRequester() }
    val osdReplayFocusRequester = remember { FocusRequester() }
    val resumeFocusRequester = remember { FocusRequester() }
    val restartFocusRequester = remember { FocusRequester() }

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

    LaunchedEffect(Unit) {
        try {
            rootFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    LaunchedEffect(isResumeDialogVisible) {
        if (isResumeDialogVisible) {
            delay(100)
            try {
                resumeFocusRequester.requestFocus()
            } catch (_: Exception) {}
        } else if (!isGenericDialogVisible && !isAdActive) {
            try {
                rootFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isAdActive, isDialogOpen) {
        if (isAdActive && !isDialogOpen) {
            delay(120)
            try {
                adSkipFocusRequester.requestFocus()
            } catch (_: Exception) {}
        } else if (!isAdActive && !isDialogOpen) {
            try {
                rootFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    BackHandler {
        if (uiState.activeDialog != null) {
            viewModel.dismissActiveDialog()
        } else {
            onBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(rootFocusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK || keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ESCAPE) {
                        if (uiState.activeDialog != null) {
                            viewModel.dismissActiveDialog()
                        } else {
                            onBack()
                        }
                        return@onPreviewKeyEvent true
                    }

                    // Dedicated Hardware Media Keys
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            viewModel.togglePlayPause()
                            return@onPreviewKeyEvent true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            viewModel.setPlaying(true)
                            viewModel.showOsdTemporarily()
                            return@onPreviewKeyEvent true
                        }
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            viewModel.setPlaying(false)
                            viewModel.showOsdTemporarily()
                            return@onPreviewKeyEvent true
                        }
                        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                            viewModel.seekDelta(10_000L)
                            return@onPreviewKeyEvent true
                        }
                        KeyEvent.KEYCODE_MEDIA_REWIND -> {
                            viewModel.seekDelta(-10_000L)
                            return@onPreviewKeyEvent true
                        }
                    }

                    // If error overlay is active, let Compose focus handle it
                    if (uiState.errorMessage != null) {
                        return@onPreviewKeyEvent false
                    }

                    // If in-video Dialog is active, let dialog D-Pad focus handle all button navigation
                    if (isDialogOpen) {
                        return@onPreviewKeyEvent false
                    }

                    // If Ad is active (and OSD is not visible): Explicitly navigate between [Bỏ qua quảng cáo] and [← Thoát]
                    if (isAdActive && !uiState.isOsdVisible) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER -> {
                                if (isBackFocused) {
                                    onBack()
                                } else {
                                    viewModel.skipAd()
                                }
                                return@onPreviewKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_UP,
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                try {
                                    adBackFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                return@onPreviewKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN,
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                try {
                                    adSkipFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                return@onPreviewKeyEvent true
                            }
                        }
                    }

                    // When video is playing with hidden OSD: pressing D-Pad key performs action and focuses corresponding button!
                    if (!uiState.isOsdVisible) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER -> {
                                viewModel.togglePlayPause()
                                try {
                                    playPauseFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                return@onPreviewKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                viewModel.seekDelta(-10_000L)
                                try {
                                    osdRewindFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                return@onPreviewKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                viewModel.seekDelta(10_000L)
                                try {
                                    osdForwardFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                return@onPreviewKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                viewModel.showOsdTemporarily()
                                try {
                                    osdBackFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                return@onPreviewKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                viewModel.showOsdTemporarily()
                                try {
                                    playPauseFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                return@onPreviewKeyEvent true
                            }
                        }
                    }

                    // When OSD is already visible, let Compose focus naturally handle navigation across buttons
                    false
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
                    seekTrigger = uiState.seekTrigger,
                    seekTargetMs = uiState.seekTargetMs,
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
                    seekTrigger = uiState.seekTrigger,
                    seekTargetMs = uiState.seekTargetMs,
                    skipAdTrigger = uiState.skipAdTrigger,
                    resumeActionTrigger = uiState.resumeActionTrigger,
                    dialogActionTrigger = uiState.dialogActionTrigger,
                    dialogDismissTrigger = uiState.dialogDismissTrigger,
                    onProgressUpdate = { pos, dur ->
                        viewModel.updateProgress(pos, dur)
                    },
                    onStreamFound = { streamUrl ->
                        viewModel.onDirectStreamFound(streamUrl)
                    },
                    onAdSkippableState = { skippable ->
                        viewModel.setAdSkippable(skippable)
                    },
                    onResumePopupDetected = { timeText ->
                        viewModel.onResumePopupDetected(timeText)
                    },
                    onDialogDetected = { id, title, msg, btns ->
                        viewModel.onDialogDetected(id, title, msg, btns)
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

            // Native Ad TV Remote Buttons (Top-Left Exit & Bottom-Right Skip Ad)
            if ((isAdActive || isDialogOpen) && !uiState.isOsdVisible) {
                // Top-Left Exit Button
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 28.dp, start = 36.dp)
                        .focusRequester(adBackFocusRequester)
                        .onFocusChanged { isBackFocused = it.isFocused }
                        .focusProperties {
                            down = if (isResumeDialogVisible) resumeFocusRequester else adSkipFocusRequester
                            right = if (isResumeDialogVisible) resumeFocusRequester else adSkipFocusRequester
                        },
                    scale = ButtonDefaults.scale(scale = 1.0f, focusedScale = 1.15f),
                    colors = ButtonDefaults.colors(
                        containerColor = DarkNavySurface.copy(alpha = 0.85f),
                        contentColor = TextSoftWhite,
                        focusedContainerColor = IceBluePrimary,
                        focusedContentColor = DeepNavyBackground
                    ),
                    border = ButtonDefaults.border(
                        border = Border(
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ),
                        focusedBorder = Border(
                            border = BorderStroke(3.dp, Color.White),
                            shape = RoundedCornerShape(8.dp)
                        )
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    Text(text = "← Thoát", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Dedicated Bottom-Right Skip Ad TV Button (directly matching user's screen)
                if (isAdActive) {
                    Button(
                        onClick = { viewModel.skipAd() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 28.dp, end = 36.dp)
                            .focusRequester(adSkipFocusRequester)
                            .onFocusChanged { isSkipFocused = it.isFocused }
                            .focusProperties {
                                up = if (isResumeDialogVisible) restartFocusRequester else adBackFocusRequester
                                left = if (isResumeDialogVisible) restartFocusRequester else adBackFocusRequester
                            },
                        scale = ButtonDefaults.scale(
                            scale = 1.0f,
                            focusedScale = 1.15f
                        ),
                        colors = ButtonDefaults.colors(
                            containerColor = DarkNavySurface.copy(alpha = 0.9f),
                            contentColor = TextSoftWhite,
                            focusedContainerColor = IceBluePrimary,
                            focusedContentColor = DeepNavyBackground
                        ),
                        border = ButtonDefaults.border(
                            border = Border(
                                border = BorderStroke(2.dp, IceBluePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ),
                            focusedBorder = Border(
                                border = BorderStroke(3.dp, Color.White),
                                shape = RoundedCornerShape(10.dp)
                            )
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = "Bỏ qua quảng cáo ⏭",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Native TV Resume Dialog ("Tiếp tục xem?")
            if (isResumeDialogVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .width(440.dp)
                            .background(
                                color = Color(0xFF1E2638),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 32.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tiếp tục xem?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bạn đã dừng tại ",
                                fontSize = 15.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = uiState.resumePopupTimeText ?: "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                        ) {
                            // Button 1: "Tiếp tục xem" (Red)
                            Button(
                                onClick = { viewModel.confirmResume() },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(resumeFocusRequester)
                                    .onFocusChanged { isResumeFocused = it.isFocused }
                                    .focusProperties {
                                        right = restartFocusRequester
                                        up = adBackFocusRequester
                                        down = if (isAdActive) adSkipFocusRequester else FocusRequester.Default
                                    },
                                scale = ButtonDefaults.scale(scale = 1.0f, focusedScale = 1.08f),
                                colors = ButtonDefaults.colors(
                                    containerColor = Color(0xFFE50914),
                                    contentColor = Color.White,
                                    focusedContainerColor = Color(0xFFFF2E3D),
                                    focusedContentColor = Color.White
                                ),
                                border = ButtonDefaults.border(
                                    border = Border(
                                        border = BorderStroke(1.dp, Color.Transparent),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                    focusedBorder = Border(
                                        border = BorderStroke(3.dp, Color.White),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                ),
                                shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = "Tiếp tục xem",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            }

                            // Button 2: "Xem lại từ đầu" (Dark Gray)
                            Button(
                                onClick = { viewModel.confirmRestart() },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(restartFocusRequester)
                                    .onFocusChanged { isRestartFocused = it.isFocused }
                                    .focusProperties {
                                        left = resumeFocusRequester
                                        up = adBackFocusRequester
                                        down = if (isAdActive) adSkipFocusRequester else FocusRequester.Default
                                        right = if (isAdActive) adSkipFocusRequester else FocusRequester.Default
                                    },
                                scale = ButtonDefaults.scale(scale = 1.0f, focusedScale = 1.08f),
                                colors = ButtonDefaults.colors(
                                    containerColor = Color(0xFF374151),
                                    contentColor = Color.White,
                                    focusedContainerColor = Color(0xFF4B5563),
                                    focusedContentColor = Color.White
                                ),
                                border = ButtonDefaults.border(
                                    border = Border(
                                        border = BorderStroke(1.dp, Color.Transparent),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                    focusedBorder = Border(
                                        border = BorderStroke(3.dp, Color.White),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                ),
                                shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = "Xem lại từ đầu",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Universal In-Video Dialog & Popup (Supports all in-video notifications, alerts, etc.)
            if (uiState.activeDialog != null) {
                UniversalTvDialog(
                    dialog = uiState.activeDialog!!,
                    onButtonClick = { btnIndex ->
                        viewModel.clickDialogButton(btnIndex)
                    },
                    backFocusRequester = adBackFocusRequester
                )
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
                onRewindClick = { viewModel.seekDelta(-10_000L) },
                onForwardClick = { viewModel.seekDelta(10_000L) },
                onReplayClick = { viewModel.seekTo(0L) },
                onTogglePlayerMode = { viewModel.togglePlayerMode() },
                onBack = onBack,
                backFocusRequester = osdBackFocusRequester,
                playPauseFocusRequester = playPauseFocusRequester,
                rewindFocusRequester = osdRewindFocusRequester,
                forwardFocusRequester = osdForwardFocusRequester,
                replayFocusRequester = osdReplayFocusRequester
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UniversalTvDialog(
    dialog: WebDialogState,
    onButtonClick: (Int) -> Unit,
    backFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val buttonRequesters = remember(dialog.id, dialog.buttons.size) {
        List(dialog.buttons.size) { FocusRequester() }
    }

    LaunchedEffect(dialog.id) {
        delay(120)
        val targetIdx = dialog.buttons.indexOfFirst { it.isPrimary }.let { if (it >= 0) it else 0 }
        if (targetIdx in buttonRequesters.indices) {
            try {
                buttonRequesters[targetIdx].requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(520.dp)
                .background(
                    color = Color(0xFF1E2638),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 32.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dialog.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            if (dialog.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = dialog.message,
                    fontSize = 15.sp,
                    color = Color(0xFFD1D5DB),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                dialog.buttons.forEachIndexed { index, btnData ->
                    Button(
                        onClick = { onButtonClick(btnData.index) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(buttonRequesters[index])
                            .focusProperties {
                                up = backFocusRequester
                                if (index > 0) {
                                    left = buttonRequesters[index - 1]
                                }
                                if (index < buttonRequesters.size - 1) {
                                    right = buttonRequesters[index + 1]
                                }
                            },
                        scale = ButtonDefaults.scale(scale = 1.0f, focusedScale = 1.08f),
                        colors = ButtonDefaults.colors(
                            containerColor = if (btnData.isPrimary) Color(0xFF2563EB) else Color(0xFF374151),
                            contentColor = Color.White,
                            focusedContainerColor = if (btnData.isPrimary) Color(0xFF3B82F6) else Color(0xFF4B5563),
                            focusedContentColor = Color.White
                        ),
                        border = ButtonDefaults.border(
                            border = Border(
                                border = BorderStroke(1.dp, Color.Transparent),
                                shape = RoundedCornerShape(8.dp)
                            ),
                            focusedBorder = Border(
                                border = BorderStroke(3.dp, Color.White),
                                shape = RoundedCornerShape(8.dp)
                            )
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = btnData.text,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
