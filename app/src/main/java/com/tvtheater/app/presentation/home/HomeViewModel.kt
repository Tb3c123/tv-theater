package com.tvtheater.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvtheater.app.domain.model.Movie
import com.tvtheater.app.domain.usecase.GetRandomCatalogUseCase
import com.tvtheater.app.domain.usecase.HomeCatalog
import com.tvtheater.app.domain.usecase.ManageHistoryUseCase
import com.tvtheater.app.domain.usecase.MovieSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val heroMovie: Movie?,
        val continueWatching: List<Movie>,
        val sections: List<MovieSection>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val getRandomCatalogUseCase: GetRandomCatalogUseCase,
    private val manageHistoryUseCase: ManageHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var cachedCatalog: HomeCatalog? = null

    init {
        loadCatalog()
        observeHistory()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            getRandomCatalogUseCase()
                .onSuccess { catalog ->
                    cachedCatalog = catalog
                    _uiState.value = HomeUiState.Success(
                        heroMovie = catalog.heroMovie,
                        continueWatching = emptyList(),
                        sections = catalog.sections
                    )
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(
                        error.localizedMessage ?: "Không thể tải danh sách phim. Vui lòng kiểm tra mạng."
                    )
                }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            manageHistoryUseCase.getHistoryList().collectLatest { entities ->
                val current = _uiState.value
                val historyMovies = entities.map { entity ->
                    Movie(
                        slug = entity.movieSlug,
                        name = entity.movieName,
                        posterUrl = entity.posterUrl,
                        thumbUrl = entity.posterUrl,
                        currentEpisode = entity.episodeName
                    )
                }
                if (current is HomeUiState.Success) {
                    _uiState.value = current.copy(continueWatching = historyMovies)
                } else if (cachedCatalog != null) {
                    _uiState.value = HomeUiState.Success(
                        heroMovie = cachedCatalog?.heroMovie,
                        continueWatching = historyMovies,
                        sections = cachedCatalog?.sections ?: emptyList()
                    )
                }
            }
        }
    }
}
