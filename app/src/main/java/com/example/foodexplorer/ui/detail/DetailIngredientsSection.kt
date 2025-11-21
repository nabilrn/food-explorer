package com.example.foodexplorer.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodexplorer.data.model.MealDetail
import com.example.foodexplorer.ui.components.IngredientItem
import com.example.foodexplorer.ui.components.SectionTitle

@Composable
fun DetailIngredientsSection(meal: MealDetail, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
    ) {
        SectionTitle(text = "Ingredients")

        // Two-column grid layout using chunked list (NOT LazyGrid!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            meal.ingredients.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { ingredient ->
                        IngredientItem(
                            name = ingredient.name,
                            measure = ingredient.measure,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Add empty space if odd number of items
                    if (rowItems.size < 2) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

