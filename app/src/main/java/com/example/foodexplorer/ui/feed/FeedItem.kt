package com.example.foodexplorer.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodexplorer.data.model.MealFeedItem
import com.example.foodexplorer.ui.components.MealImage
import com.example.foodexplorer.ui.components.MealTitle

@Composable
fun FeedItem(
    meal: MealFeedItem,
    isSaved: Boolean,
    onMealClick: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onShare: (MealFeedItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { meal.idMeal?.let { onMealClick(it) } }
    ) {
        // Full-width image with consistent aspect ratio
        MealImage(
            imageUrl = meal.strMealThumb,
            aspectRatio = 1.15f,
            showGradient = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Title and action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Title and category/origin column
            Column(modifier = Modifier.weight(1f)) {
                // Large, bold title
                MealTitle(
                    name = meal.strMeal,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category and origin subtitle
                val subtitle = buildString {
                    if (!meal.strCategory.isNullOrEmpty()) {
                        append(meal.strCategory)
                    }
                    if (!meal.strArea.isNullOrEmpty()) {
                        if (isNotEmpty()) append(" • ")
                        append(meal.strArea)
                    }
                }

                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Minimal action icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { meal.idMeal?.let { onToggleSave(it) } },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from saved" else "Save meal",
                        modifier = Modifier.size(20.dp),
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subtle divider
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 0.5.dp
        )
    }
}

