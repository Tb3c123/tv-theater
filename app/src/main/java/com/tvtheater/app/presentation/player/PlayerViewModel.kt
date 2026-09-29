package com.tvtheater.app.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvtheater.app.domain.usecase.ExtractStreamUrlUseCase
import com.tvtheater.app.domain.usecase.ManageHistoryUseCase
import com.tvtheater.app.domain.usecase.StreamResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PlayerMode {
    NATIVE_EXOPLAYER,
    WEBVIEW_FALLBACK
}

data class PlayerUiState(
    val movieSlug: String = "",
    val movieName: String = "",
    val posterUrl: String? = null,
    val episodeSlug: String = "",
    val episodeName: String = "",
    val embedUrl: String = "",
    val streamUrl: String? = null,
    val playerMode: PlayerMode = PlayerMode.NATIVE_EXOPLAYER,
    val isPlaying: Boolean = true,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isOsdVisible: Boolean = true,
    val errorMessage: String? = null
)

class PlayerViewModel(
    private val extractStreamUrlUseCase: ExtractStreamUrlUseCase,
    private val manageHistoryUseCase: ManageHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var lastSavedPositionMs = 0L
    private var osdHideJob: Job? = null
    private var bufferingTimeoutJob: Job? = null

    fun initPlayer(
        movieSlug: String,
        movieName: String,
        posterUrl: String?,
        episodeSlug: String,
        episodeName: String,
        embedUrl: String,
        initialPositionMs: Long = 0L
    ) {
        _uiState.value = _uiState.value.copy(
            movieSlug = movieSlug,
            movieName = movieName,
            posterUrl = posterUrl,
            episodeSlug = episodeSlug,
            episodeName = episodeName,
            embedUrl = embedUrl,
            currentPositionMs = initialPositionMs,
            isPlaying = true,
            isBuffering = true,
            errorMessage = null
        )
        lastSavedPositionMs = initialPositionMs

        bufferingTimeoutJob?.cancel()
        bufferingTimeoutJob = viewModelScope.launch {
            delay(18_000L)
            if (_uiState.value.isBuffering || (_uiState.value.currentPositionMs == 0L && _uiState.value.errorMessage == null)) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Video tải lâu hơn dự kiến từ máy chủ nguồn. Bạn có thể tải lại trang hoặc quay lại chọn tập phim khác.",
                    isBuffering = false
                )
            }
        }

        viewModelScope.launch {
            val result = extractStreamUrlUseCase(embedUrl)
            when (result) {
                is StreamResult.DirectHls -> {
                    _uiState.value = _uiState.value.copy(
                        playerMode = PlayerMode.NATIVE_EXOPLAYER,
                        streamUrl = result.streamUrl,
                        isBuffering = false
                    )
                }
                is StreamResult.FallbackEmbed -> {
                    _uiState.value = _uiState.value.copy(
                        playerMode = PlayerMode.WEBVIEW_FALLBACK,
                        streamUrl = null,
                        isBuffering = false
                    )
                }
            }
            showOsdTemporarily()
        }
    }

    fun onDirectStreamFound(url: String) {
        bufferingTimeoutJob?.cancel()
        if (_uiState.value.streamUrl != url) {
            _uiState.value = _uiState.value.copy(
                streamUrl = url,
                playerMode = PlayerMode.NATIVE_EXOPLAYER,
                isBuffering = false
            )
        }
    }

    fun togglePlayPause() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
        showOsdTemporarily()
    }

    fun setPlaying(playing: Boolean) {
        _uiState.value = _uiState.value.copy(isPlaying = playing)
        if (playing) showOsdTemporarily()
    }

    fun setBuffering(buffering: Boolean) {
        _uiState.value = _uiState.value.copy(isBuffering = buffering)
        if (!buffering && _uiState.value.isPlaying) {
            showOsdTemporarily()
        }
    }

    fun togglePlayerMode() {
        val newMode = if (_uiState.value.playerMode == PlayerMode.NATIVE_EXOPLAYER) {
            PlayerMode.WEBVIEW_FALLBACK
        } else {
            PlayerMode.NATIVE_EXOPLAYER
        }
        _uiState.value = _uiState.value.copy(playerMode = newMode)
        showOsdTemporarily()
    }

    fun updateProgress(positionMs: Long, durationMs: Long) {
        if (positionMs > 0L) {
            bufferingTimeoutJob?.cancel()
        }
        _uiState.value = _uiState.value.copy(
            currentPositionMs = positionMs,
            durationMs = durationMs
        )

        // Auto-save history every 5 seconds (5000ms threshold)
        if (kotlin.math.abs(positionMs - lastSavedPositionMs) >= 5000L && durationMs > 0L) {
            lastSavedPositionMs = positionMs
            saveProgressToDatabase(positionMs, durationMs)
        }
    }

    private fun saveProgressToDatabase(positionMs: Long, durationMs: Long) {
        val current = _uiState.value
        if (current.movieSlug.isNotBlank()) {
            viewModelScope.launch {
                manageHistoryUseCase.updateProgress(
                    movieSlug = current.movieSlug,
                    movieName = current.movieName,
                    posterUrl = current.posterUrl,
                    episodeSlug = current.episodeSlug,
                    episodeName = current.episodeName,
                    serverName = "Default",
                    positionMs = positionMs,
                    durationMs = durationMs
                )
            }
        }
    }

    fun showOsdTemporarily(durationMs: Long = 4000L) {
        _uiState.value = _uiState.value.copy(isOsdVisible = true)
        osdHideJob?.cancel()
        // Only auto-hide if actively playing, not buffering, and no error
        if (_uiState.value.isPlaying && !_uiState.value.isBuffering && _uiState.value.errorMessage == null) {
            osdHideJob = viewModelScope.launch {
                delay(durationMs)
                if (_uiState.value.isPlaying && !_uiState.value.isBuffering && _uiState.value.errorMessage == null) {
                    _uiState.value = _uiState.value.copy(isOsdVisible = false)
                }
            }
        }
    }

    fun toggleOsd() {
        if (_uiState.value.isOsdVisible) {
            osdHideJob?.cancel()
            _uiState.value = _uiState.value.copy(isOsdVisible = false)
        } else {
            showOsdTemporarily()
        }
    }

    fun onPlayerError(message: String) {
        bufferingTimeoutJob?.cancel()
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isBuffering = false,
            playerMode = PlayerMode.WEBVIEW_FALLBACK
        )
    }

    fun retry() {
        val current = _uiState.value
        initPlayer(
            movieSlug = current.movieSlug,
            movieName = current.movieName,
            posterUrl = current.posterUrl,
            episodeSlug = current.episodeSlug,
            episodeName = current.episodeName,
            embedUrl = current.embedUrl,
            initialPositionMs = current.currentPositionMs
        )
    }
}
