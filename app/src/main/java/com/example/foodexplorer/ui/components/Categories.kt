package com.example.foodexplorer.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodexplorer.data.model.Category

@Composable
fun Categories(
    categories: List<Category>,
    modifier: Modifier = Modifier,
    selectedCategory: String? = null,
    onCategoryClick: (String?) -> Unit = {}
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = categories,
            key = { category -> category.idCategory ?: category.strCategory ?: category.hashCode() }
        ) { category ->
            val categoryName = category.strCategory.orEmpty()
            CategoryChip(
                text = categoryName,
                selected = categoryName == selectedCategory,
                onClick = {
                    onCategoryClick(
                        if (categoryName == selectedCategory) null else categoryName
                    )
                }
            )
        }
    }
}

