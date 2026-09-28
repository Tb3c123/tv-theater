package com.tvtheater.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme

val NavyDark = Color(0xFF0B1120)
val IceBlue = Color(0xFF38BDF8)
val SoftWhite = Color(0xFFF8FAFC)

@OptIn(ExperimentalTvMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = NavyDark,
                    primary = IceBlue,
                    onBackground = SoftWhite
                )
            ) {
                TVTheaterApp()
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TVTheaterApp() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "TV Theater - Rạp Phim Gia Đình",
            color = IceBlue,
            fontSize = 32.sp
        )
    }
}
