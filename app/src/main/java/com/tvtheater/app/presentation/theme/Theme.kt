package com.tvtheater.app.presentation.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
private val TVTheaterColorScheme = darkColorScheme(
    primary = IceBluePrimary,
    onPrimary = DeepNavyBackground,
    primaryContainer = DeepCyanAccent,
    onPrimaryContainer = TextSoftWhite,
    secondary = SkyBlueSecondary,
    onSecondary = DeepNavyBackground,
    background = DeepNavyBackground,
    onBackground = TextSoftWhite,
    surface = DarkNavySurface,
    onSurface = TextSoftWhite,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextMutedGray,
    error = ErrorRed,
    onError = TextSoftWhite
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TVTheaterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TVTheaterColorScheme,
        content = content
    )
}
