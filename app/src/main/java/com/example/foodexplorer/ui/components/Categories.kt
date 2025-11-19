package com.example.foodexplorer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodexplorer.data.model.Category

@Composable
fun Categories(
    categories: List<Category>,
    selectedCategory: String? = null,
    onCategoryClick: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(0.dp)
        ) {
            items(categories) { category ->
                val categoryName = category.strCategory
                CategoryItem(
                    category = category,
                    isSelected = categoryName == selectedCategory,
                    onClick = {
                        // Toggle selection - if already selected, deselect
                        onCategoryClick(
                            if (categoryName == selectedCategory) null else categoryName
                        )
                    }
                )
            }
        }
    }
}

