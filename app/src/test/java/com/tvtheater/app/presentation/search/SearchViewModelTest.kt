package com.tvtheater.app.presentation.search

import com.tvtheater.app.domain.model.Movie
import com.tvtheater.app.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: MovieRepository = mockk()
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun typingCharactersUpdatesQuery() {
        viewModel.onCharClick('A')
        viewModel.onCharClick('V')
        viewModel.onCharClick('A')
        viewModel.onCharClick('T')
        viewModel.onCharClick('A')
        viewModel.onCharClick('R')
        assertEquals("AVATAR", viewModel.query.value)

        viewModel.onBackspace()
        assertEquals("AVATA", viewModel.query.value)

        viewModel.onSpace()
        assertEquals("AVATA ", viewModel.query.value)

        viewModel.onClear()
        assertEquals("", viewModel.query.value)
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
    }

    @Test
    fun debouncedSearchReturnsResults() = runTest {
        val movies = listOf(
            Movie(slug = "dune-2", name = "Dune 2", quality = "FHD")
        )
        coEvery { repository.searchMovies("dune", 1) } returns Result.success(movies)

        viewModel.onQueryChange("dune")
        advanceTimeBy(400) // advance past debounce delay
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Success)
        assertEquals(1, (state as SearchUiState.Success).results.size)
        assertEquals("Dune 2", state.results[0].name)
    }

    @Test
    fun emptySearchResultsReturnsEmptyState() = runTest {
        coEvery { repository.searchMovies("nonexistent", 1) } returns Result.success(emptyList())

        viewModel.onQueryChange("nonexistent")
        advanceTimeBy(400)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Empty)
        assertEquals("nonexistent", (state as SearchUiState.Empty).query)
    }
}
