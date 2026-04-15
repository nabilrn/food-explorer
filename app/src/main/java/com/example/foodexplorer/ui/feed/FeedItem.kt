package com.example.foodexplorer.ui.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.foodexplorer.data.model.MealFeedItem
import com.example.foodexplorer.ui.components.MealCard

@Composable
fun FeedItem(
    meal: MealFeedItem,
    isSaved: Boolean,
    onMealClick: (String) -> Unit,
    onToggleSave: (MealFeedItem) -> Unit,
    modifier: Modifier = Modifier
) {
    MealCard(
        meal = meal,
        isSaved = isSaved,
        onMealClick = onMealClick,
        onToggleSave = onToggleSave,
        modifier = modifier
    )
}
