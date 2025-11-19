package com.example.foodexplorer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.foodexplorer.data.model.Category
import com.example.foodexplorer.ui.theme.*

// Helper function to get emoji and color for category
fun getCategoryStyle(category: String): Pair<String, Color> {
    return when (category.lowercase()) {
        "beef" -> "🍖" to CategoryBeef
        "chicken" -> "🍗" to CategoryChicken
        "dessert" -> "🍰" to CategoryDessert
        "seafood" -> "🐟" to CategorySeafood
        "vegetarian", "vegan" -> "🥬" to CategoryVegan
        "breakfast" -> "☕" to CategoryBreakfast
        "lamb" -> "🥩" to CategoryLamb
        "pasta" -> "🍝" to CategoryPasta
        "pork" -> "🥓" to CategoryPork
        else -> "🍽️" to BorderLight
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (emoji, backgroundColor) = getCategoryStyle(category.strCategory ?: "")
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .padding(end = 8.dp)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, PrimaryBlack, shape)
                } else {
                    Modifier
                }
            )
            .background(
                color = backgroundColor,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = category.strCategory ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


