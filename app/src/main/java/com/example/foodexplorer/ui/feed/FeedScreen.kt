package com.example.foodexplorer.ui.feed

import androidx.compose.foundation.gestures.ScrollableDefaults
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodexplorer.data.model.MealFeedItem
import com.example.foodexplorer.ui.components.Categories
import com.example.foodexplorer.ui.components.SearchBar
import com.example.foodexplorer.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedUiState,
    searchQuery: String,
    onMealClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleSave: (MealFeedItem) -> Unit,
    onCategoryClick: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearchMode: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Detect scroll position for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

            val threshold = 5
            totalItems > 0 && lastVisibleItem >= (totalItems - threshold)
        }
    }

    // Trigger load more when condition changes
    LaunchedEffect(shouldLoadMore, state) {
        if (shouldLoadMore && state is FeedUiState.Success) {
            if (!state.isLoading &&
                !state.isSearching &&
                state.selectedCategory == null &&
                state.meals.isNotEmpty()) {
                onLoadMore()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        val successState = state as? FeedUiState.Success

        if (successState?.isSearching == true) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = {},
                onClose = onToggleSearchMode,
                placeholder = "Type to search (min. 2 characters)..."
            )
        } else {
            TopBar(
                title = "Food Explorer",
                onSearchClick = onToggleSearchMode,
                isSearchMode = false
            )
        }

        when (state) {
            is FeedUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            is FeedUiState.Error -> {
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onRefresh) { Text(text = "Retry") }
                    }
                }
            }
            is FeedUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        flingBehavior = ScrollableDefaults.flingBehavior()
                    ) {
                        if (!state.isSearching) {
                            item(
                                key = "categories",
                                contentType = "header"
                            ) {
                                Categories(
                                    categories = state.categories,
                                    selectedCategory = state.selectedCategory,
                                    onCategoryClick = onCategoryClick
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        if (state.meals.isEmpty() && !state.isLoading) {
                            item(
                                key = "empty",
                                contentType = "empty"
                            ) {
                                EmptyState(isSearching = state.isSearching)
                            }
                        } else {
                            itemsIndexed(
                                items = state.meals,
                                key = { index, meal ->
                                    // Combine index with idMeal to ensure uniqueness
                                    // Random API can return duplicate meal IDs
                                    "${meal.idMeal ?: "unknown"}-$index"
                                },
                                contentType = { _, _ -> "meal_item" }
                            ) { _, meal ->
                                FeedItem(
                                    meal = meal,
                                    isSaved = meal.idMeal?.let { it in state.savedMealIds } == true,
                                    onMealClick = onMealClick,
                                    onToggleSave = { onToggleSave(meal) }
                                )
                            }
                        }

                        if (state.isLoading && state.meals.isNotEmpty()) {
                            item(
                                key = "loading",
                                contentType = "loader"
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(isSearching: Boolean) {
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
            if (isSearching) {
                Icon(
                    imageVector = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = if (isSearching) "No meals found" else "No meals available",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSearching) "Try a different search term" else "Pull down to refresh",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
