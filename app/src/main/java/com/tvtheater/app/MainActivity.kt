package com.tvtheater.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.tvtheater.app.presentation.navigation.TVNavGraph
import com.tvtheater.app.presentation.theme.DeepNavyBackground
import com.tvtheater.app.presentation.theme.TVTheaterTheme

@OptIn(ExperimentalTvMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as TVTheaterApplication).appContainer

        setContent {
            TVTheaterTheme {
                val navController = rememberNavController()
                TVNavGraph(
                    navController = navController,
                    appContainer = appContainer,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepNavyBackground)
                )
            }
        }
    }
}
