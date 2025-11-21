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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    onToggleSave: (MealFeedItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Remember subtitle to prevent recreation on every recomposition
    val subtitle = remember(meal.strCategory, meal.strArea) {
        buildString {
            if (!meal.strCategory.isNullOrEmpty()) {
                append(meal.strCategory)
            }
            if (!meal.strArea.isNullOrEmpty()) {
                if (isNotEmpty()) append(" • ")
                append(meal.strArea)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { meal.idMeal?.let { onMealClick(it) } }
    ) {
        MealImage(
            imageUrl = meal.strMealThumb,
            aspectRatio = 1.15f,
            showGradient = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MealTitle(
                    name = meal.strMeal,
                    modifier = Modifier.fillMaxWidth()
                )

                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Only save button, no share
            IconButton(
                onClick = { onToggleSave(meal) },
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

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 0.5.dp
        )
    }
}
