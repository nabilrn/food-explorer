package com.example.foodexplorer.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
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
    onLoadMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    // Debounced search - auto search after user stops typing for 600ms
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty() && searchQuery.length >= 2) {
            delay(600) // Wait 600ms after user stops typing
            onSearchMeals(searchQuery)
        }
    }

    // Reset refreshing state when data is loaded
    LaunchedEffect(state) {
        if (state is FeedUiState.Success) {
            isRefreshing = false
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // TopBar with search icon or Search input
        val currentState = state as? FeedUiState.Success

        if (currentState?.isSearching == true) {
            // Show search bar in header when search mode is active
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    // Manual search when Enter is pressed
                    if (searchQuery.isNotEmpty()) {
                        onSearchMeals(searchQuery)
                    }
                },
                onClose = {
                    searchQuery = ""
                    onToggleSearchMode()
                },
                placeholder = "Type to search (min. 2 characters)..."
            )
        } else {
            // Show normal TopBar with search icon
            TopBar(
                title = "Food Explorer",
                onSearchClick = {
                    onToggleSearchMode()
                },
                isSearchMode = false
            )
        }

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
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        onRefresh()
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
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
            }

            is FeedUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        onRefresh()
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {

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
                            // Show empty state if no meals found
                            if (state.meals.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(64.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if (state.isSearching) {
                                                Icon(
                                                    imageVector = Icons.Outlined.SearchOff,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(64.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                            }
                                            Text(
                                                text = if (state.isSearching) "No meals found" else "No meals available",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (state.isSearching)
                                                    "Try a different search term"
                                                else
                                                    "Pull down to refresh",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Show meals list
                                items(state.meals) { meal ->
                                    FeedItem(
                                        meal = meal,
                                        isSaved = meal.idMeal in state.savedMealIds,
                                        onMealClick = onMealClick,
                                        onToggleSave = onToggleSave,
                                        onShare = onShareMeal
                                    )
                                }

                                // Load more indicator and trigger
                                if (!state.isSearching && state.selectedCategory == null) {
                                    item {
                                        if (state.isLoadingMore) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    strokeWidth = 2.dp,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            // Trigger load more when this item is visible
                                            LaunchedEffect(Unit) {
                                                onLoadMore()
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
