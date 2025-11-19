package com.example.foodexplorer.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Feed : BottomNavItem(Screen.Feed.route, "Feed", Icons.Default.Home)
    object Saved : BottomNavItem(Screen.Saved.route, "Saved", Icons.Default.Favorite)
}