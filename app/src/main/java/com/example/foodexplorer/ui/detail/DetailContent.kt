package com.example.foodexplorer.ui.detail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import com.example.foodexplorer.data.model.MealDetail
import com.example.foodexplorer.ui.components.IngredientRow
import com.example.foodexplorer.ui.components.MealImage
import com.example.foodexplorer.ui.components.SectionHeader

@Composable
fun DetailContent(
    meal: MealDetail,
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var expandedInstructions by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(meal.idMeal) {
        showContent = false
        showContent = true
    }

    // Use State objects (not `by` delegation) so graphicsLayer reads happen at draw time,
    // avoiding recomposition on every scroll pixel.
    val topBarProgressState = remember {
        derivedStateOf {
            val raw = if (listState.firstVisibleItemIndex > 0) 1f
            else listState.firstVisibleItemScrollOffset / 260f
            raw.coerceIn(0f, 1f)
        }
    }
    val imageParallaxState = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 78f
            else listState.firstVisibleItemScrollOffset * 0.3f
        }
    }

    val shareMeal = {
        val shareText = buildString {
            append("Check out this recipe: ${meal.strMeal}\n\n")
            meal.strCategory?.takeIf { it.isNotBlank() }?.let { append("Category: $it\n") }
            meal.strArea?.takeIf { it.isNotBlank() }?.let { append("Origin: $it\n") }
            meal.strYoutube?.takeIf { it.isNotBlank() }?.let { append("\nWatch video: $it") }
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share recipe")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ── Scrollable content ──────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item(key = "hero") {
                // Image only — no buttons inside LazyColumn to avoid touch conflicts
                Box {
                    MealImage(
                        imageUrl = meal.strMealThumb,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { translationY = -imageParallaxState.value },
                        aspectRatio = 4f / 3f,
                        showGradient = false,
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                                )
                            )
                    )
                }
            }

            item(key = "meal_info") {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(300, delayMillis = 50)) + slideInVertically(tween(300, delayMillis = 50)) { it / 8 },
                    exit = fadeOut(tween(200))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                        Text(
                            text = meal.strMeal.orEmpty(),
                            style = MaterialTheme.typography.displayLarge.copy(lineHeight = 38.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            meal.strCategory?.takeIf { it.isNotBlank() }?.let {
                                DetailTag(
                                    text = it,
                                    background = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    textColor = MaterialTheme.colorScheme.primary
                                )
                            }
                            meal.strArea?.takeIf { it.isNotBlank() }?.let {
                                DetailTag(
                                    text = it,
                                    background = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item(key = "ingredients_header") {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(320, delayMillis = 120)) + slideInVertically(tween(320, delayMillis = 120)) { it / 8 },
                    exit = slideOutVertically(tween(180)) { it / 10 } + fadeOut(tween(160))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(text = "Ingredients")
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            items(
                count = meal.ingredients.size,
                key = { index -> "ingredient_$index" }
            ) { index ->
                val ingredient = meal.ingredients[index]
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    IngredientRow(
                        ingredient = ingredient.name,
                        measure = ingredient.measure
                    )
                    if (index < meal.ingredients.lastIndex) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            item(key = "instructions") {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(360, delayMillis = 180)) + slideInVertically(tween(360, delayMillis = 180)) { it / 8 }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp)) {
                        SectionHeader(text = "Instructions")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = meal.strInstructions.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (expandedInstructions) Int.MAX_VALUE else 8
                        )
                        if (!meal.strInstructions.isNullOrBlank()) {
                            TextButton(onClick = { expandedInstructions = !expandedInstructions }) {
                                Text(if (expandedInstructions) "Read less" else "Read more")
                            }
                        }
                    }
                }
            }

            if (!meal.strYoutube.isNullOrBlank()) {
                item(key = "youtube") {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, meal.strYoutube.toUri())
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Watch Video Tutorial",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // ── Persistent overlay: fading background + always-clickable buttons ─
        // Buttons live HERE (outside LazyColumn) so scroll detection never
        // interferes with tap events.
        val surfaceColor = MaterialTheme.colorScheme.surface
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .zIndex(2f)
        ) {
            // Background that fades in as user scrolls
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .graphicsLayer { alpha = topBarProgressState.value }
                    .background(surfaceColor.copy(alpha = 0.95f))
            )

            // Action row — always rendered at full alpha so buttons are always tappable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleActionButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
                Text(
                    text = meal.strMeal.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurfaceColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .graphicsLayer { alpha = topBarProgressState.value }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircleActionButton(
                        icon = Icons.Outlined.Share,
                        contentDescription = "Share",
                        onClick = shareMeal
                    )
                    CircleActionButton(
                        icon = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from saved" else "Save recipe",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else onSurfaceColor,
                        onClick = onToggleSave
                    )
                }
            }
        }
    }
}
