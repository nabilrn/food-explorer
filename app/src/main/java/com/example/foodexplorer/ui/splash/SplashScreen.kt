package com.example.foodexplorer.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodexplorer.ui.feed.FeedUiState
import com.example.foodexplorer.ui.theme.WarmBackgroundAlt
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    feedState: FeedUiState,
    modifier: Modifier = Modifier
) {
    val iconScale = remember { Animatable(0.8f) }
    val iconAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(15f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val loaderAlpha = remember { Animatable(0f) }
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (started) return@LaunchedEffect
        started = true
        iconAlpha.animateTo(1f, tween(600))
        iconScale.animateTo(1f, tween(600, easing = EaseOutBack))
        delay(200)
        titleAlpha.animateTo(1f, tween(400, easing = LinearOutSlowInEasing))
        titleOffset.animateTo(0f, tween(400, easing = LinearOutSlowInEasing))
        delay(150)
        subtitleAlpha.animateTo(1f, tween(400))
        delay(150)
        loaderAlpha.animateTo(1f, tween(350))
    }

    LaunchedEffect(feedState) {
        if (feedState is FeedUiState.Success || feedState is FeedUiState.Error) {
            delay(500)
            onSplashFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        WarmBackgroundAlt
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(82.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Food Explorer",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .offset(y = titleOffset.value.dp)
                    .alpha(titleAlpha.value)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Discover Delicious Recipes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
            Spacer(modifier = Modifier.height(28.dp))
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .alpha(loaderAlpha.value),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
}
