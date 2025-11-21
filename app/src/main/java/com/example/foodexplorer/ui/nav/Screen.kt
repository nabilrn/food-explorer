package com.example.foodexplorer.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Splash : Screen("splash", "Splash", Icons.Default.Home)
    object Feed : Screen("feed", "Feed", Icons.Default.Home)
    object Saved : Screen("saved", "Saved", Icons.Default.Favorite)
}