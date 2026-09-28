package com.tvtheater.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.tvtheater.app.domain.model.Movie
import com.tvtheater.app.presentation.components.HeroBanner
import com.tvtheater.app.presentation.components.MovieRow
import com.tvtheater.app.presentation.components.TVTopBar
import com.tvtheater.app.presentation.theme.CardBackground
import com.tvtheater.app.presentation.theme.DeepNavyBackground
import com.tvtheater.app.presentation.theme.IceBluePrimary
import com.tvtheater.app.presentation.theme.TextMutedGray
import com.tvtheater.app.presentation.theme.TextSoftWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (Movie) -> Unit,
    onNavigateSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = IceBluePrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đang tải danh mục phim...",
                            color = TextMutedGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    ) {
                        Text(
                            text = "Không thể tải dữ liệu",
                            color = TextSoftWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            color = TextMutedGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadCatalog() },
                            colors = ButtonDefaults.colors(
                                containerColor = IceBluePrimary,
                                contentColor = DeepNavyBackground
                            ),
                            shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                        ) {
                            Text(text = "Thử Lại", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is HomeUiState.Success -> {
                TvLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 48.dp)
                ) {
                    // Top Navigation Bar
                    item {
                        TVTopBar(
                            currentRoute = "home",
                            onNavigateHome = { /* Already at home */ },
                            onNavigateSearch = onNavigateSearch,
                            onShuffleRefresh = { viewModel.loadCatalog() }
                        )
                    }

                    // Featured Hero Banner
                    item {
                        HeroBanner(
                            movie = state.heroMovie,
                            onPlayClick = { movie -> onMovieClick(movie) },
                            onDetailClick = { movie -> onMovieClick(movie) }
                        )
                    }

                    // Continue Watching Rail (if any)
                    if (state.continueWatching.isNotEmpty()) {
                        item {
                            MovieRow(
                                title = "Tiếp Tục Xem",
                                movies = state.continueWatching,
                                onMovieClick = onMovieClick
                            )
                        }
                    }

                    // Dynamic Categorized and Shuffled Rows
                    items(state.sections, key = { it.title }) { section ->
                        MovieRow(
                            title = section.title,
                            movies = section.movies,
                            onMovieClick = onMovieClick
                        )
                    }
                }
            }
        }
    }
}
