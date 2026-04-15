package com.example.foodexplorer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.foodexplorer.ui.theme.WarmOutline

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape
) {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            WarmOutline.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.85f),
            WarmOutline.copy(alpha = 0.45f)
        ),
        start = Offset(progress.value - 300f, progress.value - 300f),
        end = Offset(progress.value, progress.value)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}
