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

data class DialogButtonData(
    val index: Int,
    val text: String,
    val isPrimary: Boolean = false
)

data class WebDialogState(
    val id: String,
    val title: String,
    val message: String,
    val buttons: List<DialogButtonData>
)

data class PlayerUiState(
    val movieSlug: String = "",
    val movieName: String = "",
    val posterUrl: String? = null,
    val episodeSlug: String = "",
    val episodeName: String = "",
    val embedUrl: String = "",
    val streamUrl: String? = null,
    val playerMode: PlayerMode = PlayerMode.WEBVIEW_FALLBACK,
    val isPlaying: Boolean = true,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isOsdVisible: Boolean = true,
    val isAdSkippable: Boolean = false,
    val skipAdTrigger: Int = 0,
    val seekTargetMs: Long? = null,
    val seekTrigger: Int = 0,
    val resumePopupTimeText: String? = null,
    val resumeActionTrigger: Int = 0,
    val activeDialog: WebDialogState? = null,
    val dialogActionTrigger: Int? = null,
    val dialogDismissTrigger: Int = 0,
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
            playerMode = PlayerMode.WEBVIEW_FALLBACK,
            currentPositionMs = initialPositionMs,
            isPlaying = true,
            isBuffering = true,
            isAdSkippable = false,
            skipAdTrigger = 0,
            resumePopupTimeText = null,
            resumeActionTrigger = 0,
            activeDialog = null,
            dialogActionTrigger = null,
            dialogDismissTrigger = 0,
            errorMessage = null
        )
        lastSavedPositionMs = initialPositionMs

        bufferingTimeoutJob?.cancel()
        bufferingTimeoutJob = viewModelScope.launch {
            delay(45_000L)
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
                        streamUrl = result.streamUrl,
                        isBuffering = false
                    )
                }
                is StreamResult.FallbackEmbed -> {
                    _uiState.value = _uiState.value.copy(
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

    fun seekDelta(deltaMs: Long) {
        val current = _uiState.value
        val newPos = if (current.durationMs > 0L) {
            (current.currentPositionMs + deltaMs).coerceIn(0L, current.durationMs)
        } else {
            (current.currentPositionMs + deltaMs).coerceAtLeast(0L)
        }
        _uiState.value = current.copy(
            currentPositionMs = newPos,
            seekTargetMs = newPos,
            seekTrigger = current.seekTrigger + 1
        )
        showOsdTemporarily()
    }

    fun seekTo(positionMs: Long) {
        val current = _uiState.value
        val newPos = if (current.durationMs > 0L) {
            positionMs.coerceIn(0L, current.durationMs)
        } else {
            positionMs.coerceAtLeast(0L)
        }
        _uiState.value = current.copy(
            currentPositionMs = newPos,
            seekTargetMs = newPos,
            seekTrigger = current.seekTrigger + 1
        )
        showOsdTemporarily()
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

    fun setAdSkippable(skippable: Boolean) {
        if (_uiState.value.isAdSkippable != skippable) {
            _uiState.value = _uiState.value.copy(isAdSkippable = skippable)
        }
    }

    fun skipAd() {
        _uiState.value = _uiState.value.copy(
            skipAdTrigger = _uiState.value.skipAdTrigger + 1,
            isAdSkippable = false
        )
    }

    fun onResumePopupDetected(timeText: String) {
        if (_uiState.value.resumePopupTimeText != timeText) {
            _uiState.value = _uiState.value.copy(resumePopupTimeText = timeText)
        }
    }

    fun confirmResume() {
        _uiState.value = _uiState.value.copy(
            resumePopupTimeText = null,
            resumeActionTrigger = 1
        )
        showOsdTemporarily()
    }

    fun confirmRestart() {
        _uiState.value = _uiState.value.copy(
            resumePopupTimeText = null,
            resumeActionTrigger = 2
        )
        showOsdTemporarily()
    }

    fun dismissResumePopup() {
        _uiState.value = _uiState.value.copy(resumePopupTimeText = null)
    }

    fun onDialogDetected(id: String, title: String, message: String, buttonsJson: String) {
        try {
            val listType = object : com.google.gson.reflect.TypeToken<List<DialogButtonData>>() {}.type
            val buttonsList: List<DialogButtonData> = com.google.gson.Gson().fromJson(buttonsJson, listType) ?: emptyList()
            if (_uiState.value.activeDialog?.id != id) {
                _uiState.value = _uiState.value.copy(
                    activeDialog = WebDialogState(
                        id = id,
                        title = title,
                        message = message,
                        buttons = buttonsList
                    )
                )
            }
        } catch (_: Exception) {}
    }

    fun clickDialogButton(buttonIndex: Int) {
        _uiState.value = _uiState.value.copy(
            activeDialog = null,
            dialogActionTrigger = buttonIndex
        )
        showOsdTemporarily()
    }

    fun dismissActiveDialog() {
        _uiState.value = _uiState.value.copy(
            activeDialog = null,
            dialogDismissTrigger = _uiState.value.dialogDismissTrigger + 1
        )
        showOsdTemporarily()
    }
}
