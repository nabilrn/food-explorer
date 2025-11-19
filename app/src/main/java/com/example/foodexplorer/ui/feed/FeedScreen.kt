package com.example.foodexplorer.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodexplorer.data.model.MealFeedItem
import com.example.foodexplorer.ui.components.Categories
import com.example.foodexplorer.ui.components.SearchBar
import com.example.foodexplorer.ui.components.TopBar

@Composable
fun FeedScreen(
    state: FeedUiState,
    onMealClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleSave: (String) -> Unit = {},
    onCategoryClick: (String?) -> Unit = {},
    onSearchMeals: (String) -> Unit = {},
    onToggleSearchMode: () -> Unit = {},
    onShareMeal: (MealFeedItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        // TopBar with search icon
        val currentState = state as? FeedUiState.Success
        TopBar(
            title = "Food Explorer",
            onSearchClick = {
                onToggleSearchMode()
                if (currentState?.isSearching == true) {
                    searchQuery = ""
                }
            },
            isSearchMode = currentState?.isSearching == true
        )

        when (state) {
            FeedUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }

            is FeedUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onRefresh) {
                        Text(text = "Retry")
                    }
                }
            }

            is FeedUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Show search bar when in search mode
                    if (state.isSearching) {
                        item {
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = { onSearchMeals(searchQuery) }
                            )
                        }
                    }

                    // Show categories only when not searching
                    if (!state.isSearching) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Categories(
                                categories = state.categories,
                                selectedCategory = state.selectedCategory,
                                onCategoryClick = onCategoryClick
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Show loading indicator when filtering category
                    if (state.isLoadingCategory) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    } else {
                        items(state.meals) { meal ->
                            FeedItem(
                                meal = meal,
                                isSaved = meal.idMeal in state.savedMealIds,
                                onMealClick = onMealClick,
                                onToggleSave = onToggleSave,
                                onShare = onShareMeal
                            )
                        }
                    }
                }
            }
        }
    }
}

