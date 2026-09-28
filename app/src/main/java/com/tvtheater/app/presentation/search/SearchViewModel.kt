package com.tvtheater.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvtheater.app.domain.model.Movie
import com.tvtheater.app.domain.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val results: List<Movie>) : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onCharClick(char: Char) {
        _query.value = _query.value + char
        performSearch(_query.value)
    }

    fun onBackspace() {
        if (_query.value.isNotEmpty()) {
            _query.value = _query.value.dropLast(1)
            performSearch(_query.value)
        }
    }

    fun onClear() {
        _query.value = ""
        _uiState.value = SearchUiState.Idle
    }

    fun onSpace() {
        _query.value = _query.value + " "
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        performSearch(newQuery)
    }

    private fun performSearch(keyword: String) {
        searchJob?.cancel()
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.value = SearchUiState.Loading
            movieRepository.searchMovies(trimmed, page = 1)
                .onSuccess { movies ->
                    if (movies.isEmpty()) {
                        _uiState.value = SearchUiState.Empty(trimmed)
                    } else {
                        _uiState.value = SearchUiState.Success(movies)
                    }
                }
                .onFailure { error ->
                    _uiState.value = SearchUiState.Error(
                        error.localizedMessage ?: "Tìm kiếm thất bại. Vui lòng thử lại."
                    )
                }
        }
    }
}
