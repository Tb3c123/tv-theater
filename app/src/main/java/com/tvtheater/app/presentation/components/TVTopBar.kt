package com.tvtheater.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.tvtheater.app.presentation.theme.CardBackground
import com.tvtheater.app.presentation.theme.DarkNavySurface
import com.tvtheater.app.presentation.theme.DeepNavyBackground
import com.tvtheater.app.presentation.theme.IceBluePrimary
import com.tvtheater.app.presentation.theme.SkyBlueSecondary
import com.tvtheater.app.presentation.theme.TextMutedGray
import com.tvtheater.app.presentation.theme.TextSoftWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TVTopBar(
    currentRoute: String,
    onNavigateHome: () -> Unit,
    onNavigateSearch: () -> Unit,
    onShuffleRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo / Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(IceBluePrimary, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "TV THEATER",
                color = TextSoftWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
        }

        // Navigation & Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Home Button
            val isHome = currentRoute == "home"
            Button(
                onClick = onNavigateHome,
                scale = ButtonDefaults.scale(focusedScale = 1.05f),
                colors = ButtonDefaults.colors(
                    containerColor = if (isHome) DarkNavySurface else Color.Transparent,
                    contentColor = if (isHome) IceBluePrimary else TextMutedGray,
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
                    text = "Trang Chủ",
                    fontSize = 13.sp,
                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Search Button
            val isSearch = currentRoute == "search"
            Button(
                onClick = onNavigateSearch,
                scale = ButtonDefaults.scale(focusedScale = 1.05f),
                colors = ButtonDefaults.colors(
                    containerColor = if (isSearch) DarkNavySurface else Color.Transparent,
                    contentColor = if (isSearch) IceBluePrimary else TextMutedGray,
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
                    text = "🔍 Tìm Kiếm",
                    fontSize = 13.sp,
                    fontWeight = if (isSearch) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Shuffle Button
            Button(
                onClick = onShuffleRefresh,
                scale = ButtonDefaults.scale(focusedScale = 1.05f),
                colors = ButtonDefaults.colors(
                    containerColor = DarkNavySurface.copy(alpha = 0.6f),
                    contentColor = SkyBlueSecondary,
                    focusedContainerColor = IceBluePrimary,
                    focusedContentColor = DeepNavyBackground
                ),
                border = ButtonDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(2.dp, SkyBlueSecondary),
                        shape = RoundedCornerShape(8.dp)
                    )
                ),
                shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "🎲 Khám Phá Mới",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
