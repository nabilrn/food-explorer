package com.example.foodexplorer.ui.theme

import androidx.compose.ui.graphics.Color

val WarmPrimary = Color(0xFFE85D2C)
val WarmSecondary = Color(0xFFF5A623)
val WarmBackground = Color(0xFFFAFAF8)
val WarmBackgroundAlt = Color(0xFFFFF8F0)
val WarmSurface = Color(0xFFFFFFFF)
val WarmOnSurface = Color(0xFF1A1A1A)
val WarmOnSurfaceVariant = Color(0xFF6B6B6B)
val WarmError = Color(0xFFD32F2F)
val WarmOutline = Color(0xFFE0DFDB)

val DarkPrimary = Color(0xFFFF8A5F)
val DarkSecondary = Color(0xFFFFCA63)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1D1D1D)
val DarkOnSurface = Color(0xFFF4F0EB)
val DarkOnSurfaceVariant = Color(0xFFC2BDB6)
val DarkOutline = Color(0xFF3D3A36)
val DarkError = Color(0xFFEF5350)

// Compatibility aliases for existing UI files that still reference old names.
val PrimaryBlack = WarmOnSurface
val BackgroundWhite = WarmBackground
val SurfaceWhite = WarmSurface
val TextBlack = WarmOnSurface
val TextGray = WarmOnSurfaceVariant
val CaptionGray = WarmOnSurfaceVariant
val AccentPurple = WarmSecondary
val AccentPurpleLight = WarmPrimary.copy(alpha = 0.12f)
val DividerColor = WarmOutline
val BorderLight = WarmOutline
val BottomNavGlass = WarmSurface

