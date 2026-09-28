package com.tvtheater.app.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvtheater.app.domain.model.MovieDetail
import com.tvtheater.app.domain.repository.MovieRepository
import com.tvtheater.app.domain.usecase.ManageHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(
        val movie: MovieDetail,
        val selectedServerIndex: Int = 0,
        val resumeEpisodeSlug: String? = null,
        val resumePositionMs: Long = 0L,
        val resumeDurationMs: Long = 0L
    ) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    private val movieRepository: MovieRepository,
    private val manageHistoryUseCase: ManageHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadMovie(slug: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val movieResult = movieRepository.getMovieDetail(slug)
            val history = manageHistoryUseCase.getHistoryForMovie(slug)

            movieResult
                .onSuccess { detail ->
                    _uiState.value = DetailUiState.Success(
                        movie = detail,
                        selectedServerIndex = 0,
                        resumeEpisodeSlug = history?.episodeSlug,
                        resumePositionMs = history?.positionMs ?: 0L,
                        resumeDurationMs = history?.durationMs ?: 0L
                    )
                }
                .onFailure { error ->
                    _uiState.value = DetailUiState.Error(
                        error.localizedMessage ?: "Không thể tải thông tin chi tiết phim."
                    )
                }
        }
    }

    fun selectServer(index: Int) {
        val current = _uiState.value
        if (current is DetailUiState.Success) {
            if (index in current.movie.servers.indices) {
                _uiState.value = current.copy(selectedServerIndex = index)
            }
        }
    }
}
