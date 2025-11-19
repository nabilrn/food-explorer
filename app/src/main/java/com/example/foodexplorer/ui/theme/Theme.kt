package com.example.foodexplorer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlack,
    onPrimary = SurfaceWhite,
    secondary = AccentPurple,
    onSecondary = PrimaryBlack,
    background = BackgroundWhite,
    onBackground = TextBlack,
    surface = SurfaceWhite,
    onSurface = TextBlack,
    surfaceVariant = BorderLight,
    onSurfaceVariant = TextGray,
    outline = DividerColor,
    outlineVariant = BorderLight
)

@Composable
fun FoodExplorerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

