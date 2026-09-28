package com.tvtheater.app.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.tvtheater.app.domain.model.EpisodeItem
import com.tvtheater.app.presentation.theme.BadgeYellow
import com.tvtheater.app.presentation.theme.CardBackground
import com.tvtheater.app.presentation.theme.DarkNavySurface
import com.tvtheater.app.presentation.theme.DeepNavyBackground
import com.tvtheater.app.presentation.theme.IceBluePrimary
import com.tvtheater.app.presentation.theme.SkyBlueSecondary
import com.tvtheater.app.presentation.theme.TextMutedGray
import com.tvtheater.app.presentation.theme.TextSoftWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailScreen(
    movieSlug: String,
    viewModel: DetailViewModel,
    onPlayEpisode: (movieSlug: String, movieName: String, posterUrl: String?, episodeSlug: String, episodeName: String, embedUrl: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(movieSlug) {
        viewModel.loadMovie(movieSlug)
    }

    LaunchedEffect(uiState) {
        if (uiState is DetailUiState.Success) {
            delay(150)
            try {
                playFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
    ) {
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IceBluePrimary)
                }
            }

            is DetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = TextMutedGray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadMovie(movieSlug) }) {
                            Text(text = "Thử Lại")
                        }
                    }
                }
            }

            is DetailUiState.Success -> {
                val movie = state.movie
                val currentServer = movie.servers.getOrNull(state.selectedServerIndex)
                val episodes = currentServer?.episodes ?: emptyList()

                // Backdrop background image with dark cinema tint
                AsyncImage(
                    model = movie.posterUrl ?: movie.thumbUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    DeepNavyBackground.copy(alpha = 0.85f),
                                    DeepNavyBackground.copy(alpha = 0.95f),
                                    DeepNavyBackground
                                )
                            )
                        )
                )

                TvLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header with Back button
                    item {
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.colors(
                                containerColor = DarkNavySurface.copy(alpha = 0.7f),
                                contentColor = TextMutedGray,
                                focusedContainerColor = IceBluePrimary,
                                focusedContentColor = DeepNavyBackground
                            ),
                            shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                        ) {
                            Text(text = "← Quay Lại", fontSize = 13.sp)
                        }
                    }

                    // Main Info Section (Poster + Metadata)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            // Poster
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .aspectRatio(2f / 3f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkNavySurface)
                            ) {
                                AsyncImage(
                                    model = movie.thumbUrl ?: movie.posterUrl,
                                    contentDescription = movie.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Movie Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = movie.name,
                                    color = TextSoftWhite,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (!movie.originalName.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = movie.originalName,
                                        color = TextMutedGray,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Tags Row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                IceBluePrimary.copy(alpha = 0.2f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = movie.quality,
                                            color = IceBluePrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (!movie.year.isNullOrBlank()) {
                                        Text(text = movie.year, color = TextMutedGray, fontSize = 13.sp)
                                    }

                                    if (!movie.time.isNullOrBlank()) {
                                        Text(text = "• ${movie.time}", color = TextMutedGray, fontSize = 13.sp)
                                    }

                                    if (!movie.currentEpisode.isNullOrBlank()) {
                                        Text(
                                            text = "• ${movie.currentEpisode}",
                                            color = BadgeYellow,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (!movie.casts.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Diễn viên: ${movie.casts}",
                                        color = TextMutedGray,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (!movie.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = movie.description,
                                        color = TextSoftWhite.copy(alpha = 0.85f),
                                        fontSize = 13.sp,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Resume / Quick Play Button
                                val defaultEpisode = episodes.firstOrNull { it.slug == state.resumeEpisodeSlug }
                                    ?: episodes.firstOrNull()

                                if (defaultEpisode != null) {
                                    val playLabel = if (state.resumeEpisodeSlug != null) {
                                        "Tiếp tục xem: ${defaultEpisode.name}"
                                    } else {
                                        "Xem ngay: ${defaultEpisode.name}"
                                    }

                                    Button(
                                        onClick = {
                                            onPlayEpisode(
                                                movie.slug,
                                                movie.name,
                                                movie.posterUrl ?: movie.thumbUrl,
                                                defaultEpisode.slug,
                                                defaultEpisode.name,
                                                defaultEpisode.embedUrl
                                            )
                                        },
                                        modifier = Modifier.focusRequester(playFocusRequester),
                                        scale = ButtonDefaults.scale(focusedScale = 1.05f),
                                        colors = ButtonDefaults.colors(
                                            containerColor = IceBluePrimary,
                                            contentColor = DeepNavyBackground,
                                            focusedContainerColor = SkyBlueSecondary,
                                            focusedContentColor = DeepNavyBackground
                                        ),
                                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                                    ) {
                                        Text(text = "▶  $playLabel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Server Selection (if multiple)
                    if (movie.servers.size > 1) {
                        item {
                            Column {
                                Text(
                                    text = "Chọn Máy Chủ / Nguồn Phát",
                                    color = TextSoftWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TvLazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(movie.servers.indices.toList()) { index ->
                                        val isSelected = index == state.selectedServerIndex
                                        Button(
                                            onClick = { viewModel.selectServer(index) },
                                            colors = ButtonDefaults.colors(
                                                containerColor = if (isSelected) IceBluePrimary else DarkNavySurface,
                                                contentColor = if (isSelected) DeepNavyBackground else TextSoftWhite,
                                                focusedContainerColor = SkyBlueSecondary,
                                                focusedContentColor = DeepNavyBackground
                                            ),
                                            shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                                        ) {
                                            Text(
                                                text = movie.servers[index].serverName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Episode List
                    if (episodes.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    text = "Danh Sách Tập Phim (${episodes.size} tập)",
                                    color = TextSoftWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                TvLazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(episodes, key = { it.slug }) { episode ->
                                        val isCurrent = episode.slug == state.resumeEpisodeSlug
                                        Card(
                                            onClick = {
                                                onPlayEpisode(
                                                    movie.slug,
                                                    movie.name,
                                                    movie.posterUrl ?: movie.thumbUrl,
                                                    episode.slug,
                                                    episode.name,
                                                    episode.embedUrl
                                                )
                                            },
                                            scale = CardDefaults.scale(focusedScale = 1.1f),
                                            border = CardDefaults.border(
                                                focusedBorder = Border(
                                                    border = BorderStroke(2.dp, IceBluePrimary),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                            ),
                                            colors = CardDefaults.colors(
                                                containerColor = if (isCurrent) IceBluePrimary.copy(alpha = 0.25f) else CardBackground,
                                                focusedContainerColor = CardBackground
                                            ),
                                            shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = episode.name,
                                                    color = if (isCurrent) SkyBlueSecondary else TextSoftWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
