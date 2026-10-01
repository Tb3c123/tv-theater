package com.tvtheater.app.presentation.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.tvtheater.app.presentation.theme.CardBackground
import com.tvtheater.app.presentation.theme.DarkNavySurface
import com.tvtheater.app.presentation.theme.DeepNavyBackground
import com.tvtheater.app.presentation.theme.IceBluePrimary
import com.tvtheater.app.presentation.theme.SkyBlueSecondary
import com.tvtheater.app.presentation.theme.TextMutedGray
import com.tvtheater.app.presentation.theme.TextSoftWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerOsdOverlay(
    isVisible: Boolean,
    movieName: String,
    episodeName: String,
    playerMode: PlayerMode,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onReplayClick: () -> Unit = {},
    onTogglePlayerMode: () -> Unit,
    onBack: () -> Unit,
    backFocusRequester: FocusRequester = remember { FocusRequester() },
    playPauseFocusRequester: FocusRequester = remember { FocusRequester() },
    rewindFocusRequester: FocusRequester = remember { FocusRequester() },
    forwardFocusRequester: FocusRequester = remember { FocusRequester() },
    replayFocusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier
) {
    val modeFocusRequester = remember { FocusRequester() }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top Scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DeepNavyBackground.copy(alpha = 0.9f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Top Header: Back & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .focusRequester(backFocusRequester)
                            .focusProperties {
                                down = playPauseFocusRequester
                                right = modeFocusRequester
                            },
                        scale = ButtonDefaults.scale(focusedScale = 1.15f),
                        colors = ButtonDefaults.colors(
                            containerColor = DarkNavySurface.copy(alpha = 0.8f),
                            contentColor = TextSoftWhite,
                            focusedContainerColor = IceBluePrimary,
                            focusedContentColor = DeepNavyBackground
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "← Thoát", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = movieName,
                            color = TextSoftWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = episodeName,
                            color = SkyBlueSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                // Mode Indicator Badge Button
                Button(
                    onClick = onTogglePlayerMode,
                    modifier = Modifier
                        .focusRequester(modeFocusRequester)
                        .focusProperties {
                            down = playPauseFocusRequester
                            left = backFocusRequester
                        },
                    scale = ButtonDefaults.scale(focusedScale = 1.08f),
                    colors = ButtonDefaults.colors(
                        containerColor = DarkNavySurface.copy(alpha = 0.8f),
                        contentColor = IceBluePrimary,
                        focusedContainerColor = SkyBlueSecondary,
                        focusedContentColor = DeepNavyBackground
                    ),
                    border = ButtonDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, IceBluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        )
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    val modeText = if (playerMode == PlayerMode.NATIVE_EXOPLAYER) {
                        "🎬 Nguồn: ExoPlayer HLS"
                    } else {
                        "🌐 Nguồn: WebView Dự Phòng"
                    }
                    Text(text = modeText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Bottom Scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DeepNavyBackground.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Bottom Timeline & Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 48.dp, vertical = 24.dp)
            ) {
                // Time progress & duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPositionMs),
                        color = TextSoftWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (durationMs > 0) formatTime(durationMs) else "--:--",
                        color = TextMutedGray,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = IceBluePrimary,
                    trackColor = DarkNavySurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button 1: Replay from 00:00
                    Button(
                        onClick = onReplayClick,
                        modifier = Modifier
                            .focusRequester(replayFocusRequester)
                            .focusProperties {
                                up = backFocusRequester
                                right = rewindFocusRequester
                            },
                        scale = ButtonDefaults.scale(scale = 1.0f, focusedScale = 1.10f),
                        colors = ButtonDefaults.colors(
                            containerColor = DarkNavySurface,
                            contentColor = TextSoftWhite,
                            focusedContainerColor = IceBluePrimary,
                            focusedContentColor = DeepNavyBackground
                        ),
                        border = ButtonDefaults.border(
                            border = Border(
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp)
                            ),
                            focusedBorder = Border(
                                border = BorderStroke(3.dp, Color.White),
                                shape = RoundedCornerShape(8.dp)
                            )
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "⏮ Xem lại (00:00)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Button 2: Rewind 10s
                    Button(
                        onClick = onRewindClick,
                        modifier = Modifier
                            .focusRequester(rewindFocusRequester)
                            .focusProperties {
                                up = backFocusRequester
                                left = replayFocusRequester
                                right = playPauseFocusRequester
                            },
                        scale = ButtonDefaults.scale(scale = 1.0f, focusedScale = 1.10f),
                        colors = ButtonDefaults.colors(
                            containerColor = DarkNavySurface,
                            contentColor = TextSoftWhite,
                            focusedContainerColor = IceBluePrimary,
                            focusedContentColor = DeepNavyBackground
                        ),
                        border = ButtonDefaults.border(
                            border = Border(
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp)
                            ),
                            focusedBorder = Border(
                                border = BorderStroke(3.dp, Color.White),
                                shape = RoundedCornerShape(8.dp)
                            )
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "⏪ -10s", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Button 3: Play/Pause
                    Button(
                        onClick = onPlayPauseClick,
                        modifier = Modifier
                            .focusRequester(playPauseFocusRequester)
                            .focusProperties {
                                up = backFocusRequester
                                left = rewindFocusRequester
                                right = forwardFocusRequester
                            },
                        scale = ButtonDefaults.scale(scale = 1.0f, focusedScale = 1.10f),
                        colors = ButtonDefaults.colors(
                            containerColor = DarkNavySurface,
                            contentColor = TextSoftWhite,
                            focusedContainerColor = IceBluePrimary,
                            focusedContentColor = DeepNavyBackground
                        ),
                        border = ButtonDefaults.border(
                            border = Border(
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
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
                            text = if (isPlaying) "⏸ Tạm Dừng" else "▶ Tiếp Tục",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Button 4: Forward 10s
                    Button(
                        onClick = onForwardClick,
                        modifier = Modifier
                            .focusRequester(forwardFocusRequester)
                            .focusProperties {
                                up = backFocusRequester
                                left = playPauseFocusRequester
                            },
                        scale = ButtonDefaults.scale(scale = 1.0f, focusedScale = 1.10f),
                        colors = ButtonDefaults.colors(
                            containerColor = DarkNavySurface,
                            contentColor = TextSoftWhite,
                            focusedContainerColor = IceBluePrimary,
                            focusedContentColor = DeepNavyBackground
                        ),
                        border = ButtonDefaults.border(
                            border = Border(
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp)
                            ),
                            focusedBorder = Border(
                                border = BorderStroke(3.dp, Color.White),
                                shape = RoundedCornerShape(8.dp)
                            )
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "+10s ⏩", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerErrorOverlay(
    errorMessage: String,
    movieName: String,
    episodeName: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val retryFocusRequester = remember { FocusRequester() }
    val backFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        try {
            retryFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                text = "⚠️",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Không thể phát video",
                color = TextSoftWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$movieName • $episodeName",
                color = SkyBlueSecondary,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = errorMessage,
                color = TextMutedGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .focusRequester(retryFocusRequester)
                        .focusProperties {
                            right = backFocusRequester
                        },
                    scale = ButtonDefaults.scale(focusedScale = 1.15f),
                    colors = ButtonDefaults.colors(
                        containerColor = IceBluePrimary,
                        contentColor = DeepNavyBackground,
                        focusedContainerColor = Color.White,
                        focusedContentColor = DeepNavyBackground
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "🔄 Tải lại trang (Thử lại)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .focusRequester(backFocusRequester)
                        .focusProperties {
                            left = retryFocusRequester
                        },
                    scale = ButtonDefaults.scale(focusedScale = 1.12f),
                    colors = ButtonDefaults.colors(
                        containerColor = DarkNavySurface,
                        contentColor = TextSoftWhite,
                        focusedContainerColor = SkyBlueSecondary,
                        focusedContentColor = DeepNavyBackground
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "← Quay lại danh sách phim",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

