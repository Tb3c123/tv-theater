package com.tvtheater.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.tvtheater.app.domain.model.Movie
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
fun HeroBanner(
    movie: Movie?,
    onPlayClick: (Movie) -> Unit,
    onDetailClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movie == null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        // Backdrop Image
        AsyncImage(
            model = movie.posterUrl ?: movie.thumbUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay: Left-to-Right for text readability & Top-to-Bottom to merge with content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DeepNavyBackground,
                            DeepNavyBackground.copy(alpha = 0.85f),
                            DeepNavyBackground.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DeepNavyBackground.copy(alpha = 0.6f),
                            DeepNavyBackground
                        )
                    )
                )
        )

        // Movie Info & Action Buttons
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Quality & Year Tag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            color = IceBluePrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = movie.quality.ifBlank { "HD" },
                        color = IceBluePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!movie.year.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = movie.year,
                        color = TextMutedGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!movie.currentEpisode.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${movie.currentEpisode}",
                        color = BadgeYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = movie.name,
                color = TextSoftWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!movie.originalName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = movie.originalName,
                    color = TextMutedGray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Play Button
                Button(
                    onClick = { onPlayClick(movie) },
                    scale = ButtonDefaults.scale(focusedScale = 1.05f),
                    colors = ButtonDefaults.colors(
                        containerColor = IceBluePrimary,
                        contentColor = DeepNavyBackground,
                        focusedContainerColor = SkyBlueSecondary,
                        focusedContentColor = DeepNavyBackground
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "▶  Xem Phim",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Detail Button
                Button(
                    onClick = { onDetailClick(movie) },
                    scale = ButtonDefaults.scale(focusedScale = 1.05f),
                    colors = ButtonDefaults.colors(
                        containerColor = DarkNavySurface.copy(alpha = 0.85f),
                        contentColor = TextSoftWhite,
                        focusedContainerColor = CardBackground,
                        focusedContentColor = IceBluePrimary
                    ),
                    border = ButtonDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, IceBluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        )
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "ℹ  Chi Tiết",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
