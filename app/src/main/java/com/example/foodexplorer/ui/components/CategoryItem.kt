package com.example.foodexplorer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.foodexplorer.data.model.Category

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CategoryChip(
        text = category.strCategory.orEmpty(),
        selected = isSelected,
        onClick = onClick,
        modifier = modifier
    )
}
