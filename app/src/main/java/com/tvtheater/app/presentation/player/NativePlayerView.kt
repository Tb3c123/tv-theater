package com.tvtheater.app.presentation.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(UnstableApi::class)
@Composable
fun NativePlayerView(
    streamUrl: String,
    isPlaying: Boolean,
    initialPositionMs: Long,
    onProgressUpdate: (positionMs: Long, durationMs: Long) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("Mozilla/5.0 (Linux; Android 12; BRAVIA 4K Build/BRAVIA_ATV4_EU) AppleWebKit/537.36")
            .setDefaultRequestProperties(mapOf("Referer" to "https://phim.nguonc.com/"))

        val hlsSourceFactory = HlsMediaSource.Factory(httpDataSourceFactory)
            .setAllowChunklessPreparation(true)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(hlsSourceFactory)
            .setSeekForwardIncrementMs(10_000L)
            .setSeekBackIncrementMs(10_000L)
            .build().apply {
                val mediaItem = MediaItem.fromUri(streamUrl)
                setMediaItem(mediaItem)
                if (initialPositionMs > 0) {
                    seekTo(initialPositionMs)
                }
                prepare()
                playWhenReady = isPlaying
            }
    }

    DisposableEffect(streamUrl) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                onError(error.localizedMessage ?: "Lỗi phát luồng video")
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Sync play/pause state
    LaunchedEffect(isPlaying) {
        if (exoPlayer.playWhenReady != isPlaying) {
            exoPlayer.playWhenReady = isPlaying
        }
    }

    // Polling progress update
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            if (exoPlayer.isPlaying) {
                val current = exoPlayer.currentPosition
                val duration = if (exoPlayer.duration != C.TIME_UNSET) exoPlayer.duration else 0L
                onProgressUpdate(current, duration)
            }
            delay(1000)
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // We use our own customized 10-foot TV OSD
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier
    )
}
