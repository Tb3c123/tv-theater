package com.tvtheater.app.presentation.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.tvtheater.app.domain.model.Movie
import com.tvtheater.app.presentation.components.MovieCard
import com.tvtheater.app.presentation.theme.CardBackground
import com.tvtheater.app.presentation.theme.DarkNavySurface
import com.tvtheater.app.presentation.theme.DeepNavyBackground
import com.tvtheater.app.presentation.theme.IceBluePrimary
import com.tvtheater.app.presentation.theme.SkyBlueSecondary
import com.tvtheater.app.presentation.theme.TextMutedGray
import com.tvtheater.app.presentation.theme.TextSoftWhite

private val KeyboardRows = listOf(
    listOf('A', 'B', 'C', 'D', 'E', 'F'),
    listOf('G', 'H', 'I', 'J', 'K', 'L'),
    listOf('M', 'N', 'O', 'P', 'Q', 'R'),
    listOf('S', 'T', 'U', 'V', 'W', 'X'),
    listOf('Y', 'Z', '1', '2', '3', '4'),
    listOf('5', '6', '7', '8', '9', '0')
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onMovieClick: (Movie) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        // Left Column: Virtual Keyboard & Search input
        Column(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
        ) {
            // Back button
            Button(
                onClick = onBack,
                colors = ButtonDefaults.colors(
                    containerColor = DarkNavySurface,
                    contentColor = TextMutedGray,
                    focusedContainerColor = IceBluePrimary,
                    focusedContentColor = DeepNavyBackground
                ),
                shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
            ) {
                Text(text = "← Trang Chủ", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Query Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavySurface, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (query.isEmpty()) "Nhập từ khóa tìm kiếm..." else query,
                    color = if (query.isEmpty()) TextMutedGray else IceBluePrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Virtual Keyboard Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in KeyboardRows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (ch in row) {
                            Card(
                                onClick = { viewModel.onCharClick(ch) },
                                scale = CardDefaults.scale(focusedScale = 1.15f),
                                border = CardDefaults.border(
                                    focusedBorder = Border(
                                        border = BorderStroke(2.dp, IceBluePrimary),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                ),
                                colors = CardDefaults.colors(
                                    containerColor = CardBackground,
                                    focusedContainerColor = IceBluePrimary
                                ),
                                shape = CardDefaults.shape(RoundedCornerShape(6.dp)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = ch.toString(),
                                        color = TextSoftWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Keys: Space, Backspace, Clear
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Space Key
                    Card(
                        onClick = { viewModel.onSpace() },
                        scale = CardDefaults.scale(focusedScale = 1.1f),
                        border = CardDefaults.border(
                            focusedBorder = Border(
                                border = BorderStroke(2.dp, IceBluePrimary),
                                shape = RoundedCornerShape(6.dp)
                            )
                        ),
                        colors = CardDefaults.colors(
                            containerColor = CardBackground,
                            focusedContainerColor = IceBluePrimary
                        ),
                        shape = CardDefaults.shape(RoundedCornerShape(6.dp)),
                        modifier = Modifier
                            .weight(2f)
                            .height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(text = "Dấu Cách", color = TextSoftWhite, fontSize = 12.sp)
                        }
                    }

                    // Backspace Key
                    Card(
                        onClick = { viewModel.onBackspace() },
                        scale = CardDefaults.scale(focusedScale = 1.1f),
                        border = CardDefaults.border(
                            focusedBorder = Border(
                                border = BorderStroke(2.dp, IceBluePrimary),
                                shape = RoundedCornerShape(6.dp)
                            )
                        ),
                        colors = CardDefaults.colors(
                            containerColor = CardBackground,
                            focusedContainerColor = IceBluePrimary
                        ),
                        shape = CardDefaults.shape(RoundedCornerShape(6.dp)),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(text = "⌫ Xóa", color = TextSoftWhite, fontSize = 12.sp)
                        }
                    }

                    // Clear Key
                    Card(
                        onClick = { viewModel.onClear() },
                        scale = CardDefaults.scale(focusedScale = 1.1f),
                        border = CardDefaults.border(
                            focusedBorder = Border(
                                border = BorderStroke(2.dp, IceBluePrimary),
                                shape = RoundedCornerShape(6.dp)
                            )
                        ),
                        colors = CardDefaults.colors(
                            containerColor = CardBackground,
                            focusedContainerColor = IceBluePrimary
                        ),
                        shape = CardDefaults.shape(RoundedCornerShape(6.dp)),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(text = "Xóa Hết", color = TextSoftWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        // Right Column: Search Results
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Dùng điều khiển từ xa (Remote D-Pad) để nhập tên phim muốn tìm",
                            color = TextMutedGray,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is SearchUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = IceBluePrimary)
                    }
                }

                is SearchUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Không tìm thấy phim nào cho \"${state.query}\"",
                            color = TextMutedGray,
                            fontSize = 15.sp
                        )
                    }
                }

                is SearchUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = TextMutedGray,
                            fontSize = 15.sp
                        )
                    }
                }

                is SearchUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Kết Quả Tìm Kiếm (${state.results.size})",
                            color = TextSoftWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        TvLazyVerticalGrid(
                            columns = TvGridCells.Fixed(3),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.results, key = { it.slug }) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onMovieClick = onMovieClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
