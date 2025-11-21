package com.example.foodexplorer.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodexplorer.data.model.Category
import com.example.foodexplorer.data.model.MealFeedItem
import com.example.foodexplorer.data.repository.MealRepository
import com.example.foodexplorer.data.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

sealed interface FeedUiState {
    object Loading : FeedUiState
    data class Success(
        val categories: List<Category> = emptyList(),
        val feedMeals: List<MealFeedItem> = emptyList(),
        val meals: List<MealFeedItem> = emptyList(),
        val savedMealIds: Set<String> = emptySet(),
        val selectedCategory: String? = null,
        val isSearching: Boolean = false,
        val searchQuery: String = "",
        val isLoading: Boolean = false,
    ) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

class FeedViewModel(private val repository: MealRepository) : ViewModel() {

    companion object {
        private const val MAX_FEED_ITEMS = 50 // Increased untuk more scrolling
        private const val LOAD_MORE_COUNT = 10 // Increased untuk faster pagination
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val MIN_SEARCH_QUERY_LENGTH = 2
    }

    private val _state = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var loadMoreJob: Job? = null
    private var searchJob: Job? = null
    private var categoryJob: Job? = null

    init {
        loadInitialData()
        observeSearchQuery()
        observeSavedMeals()
    }

    // Light deduplication - hanya remove consecutive duplicates
    private fun dedupe(meals: List<MealFeedItem>): List<MealFeedItem> {
        if (meals.isEmpty()) return meals

        val result = mutableListOf<MealFeedItem>()
        var lastId: String? = null

        for (meal in meals) {
            val currentId = meal.idMeal ?: meal.strMeal
            // Only skip exact consecutive duplicates
            if (currentId != lastId) {
                result.add(meal)
                lastId = currentId
            }
        }

        // Return up to MAX_FEED_ITEMS, but don't enforce too strictly
        return result.take(MAX_FEED_ITEMS)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = FeedUiState.Loading

            try {
                val categoriesDeferred = async { repository.getCategories() }
                val mealsDeferred = async { repository.getHomeFeed() }

                val catRes = categoriesDeferred.await()
                val feedRes = mealsDeferred.await()

                if (catRes is Resource.Success && feedRes is Resource.Success) {
                    val optimizedMeals = dedupe(feedRes.data)

                    _state.value = FeedUiState.Success(
                        categories = catRes.data,
                        feedMeals = optimizedMeals,
                        meals = optimizedMeals
                    )
                } else {
                    val errorMessage = (catRes as? Resource.Error)?.message
                        ?: (feedRes as? Resource.Error)?.message
                        ?: "Failed to load feed"
                    _state.value = FeedUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _state.value = FeedUiState.Error("Error loading feed: ${e.message}")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        val current = _state.value
        if (current is FeedUiState.Success) {
            _state.value = current.copy(searchQuery = query)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .filter { _ ->
                    val current = _state.value
                    current is FeedUiState.Success && current.isSearching
                }
                .mapLatest { query ->
                    searchJob?.cancel()

                    if (query.length < MIN_SEARCH_QUERY_LENGTH) {
                        return@mapLatest null to query
                    }

                    val current = _state.value as? FeedUiState.Success ?: return@mapLatest null to query
                    _state.value = current.copy(isLoading = true)

                    repository.searchMeals(query) to query
                }
                .catch { _ ->
                    val current = _state.value as? FeedUiState.Success
                    if (current != null) {
                        _state.value = current.copy(isLoading = false)
                    }
                }
                .collect { (result, query) ->
                    val current = _state.value as? FeedUiState.Success ?: return@collect

                    if (result == null || query.length < MIN_SEARCH_QUERY_LENGTH) {
                        // Restore feed
                        _state.value = current.copy(
                            meals = current.feedMeals,
                            isLoading = false
                        )
                        return@collect
                    }

                    when (result) {
                        is Resource.Success -> {
                            val searchResults = result.data
                                .asSequence()
                                .map { detail ->
                                    MealFeedItem(
                                        idMeal = detail.idMeal,
                                        strMeal = detail.strMeal,
                                        strMealThumb = detail.strMealThumb,
                                        strCategory = detail.strCategory,
                                        strArea = detail.strArea
                                    )
                                }
                                .let { dedupe(it.toList()) }

                            _state.value = current.copy(
                                meals = searchResults,
                                isLoading = false
                            )
                        }
                        is Resource.Error -> {
                            _state.value = FeedUiState.Error(result.message)
                        }
                        is Resource.Loading -> {
                            _state.value = current.copy(isLoading = true)
                        }
                    }
                }
        }
    }

    private fun observeSavedMeals() {
        viewModelScope.launch {
            repository.getSavedMeals().collect { savedDetails ->
                val current = _state.value
                if (current is FeedUiState.Success) {
                    _state.value = current.copy(
                        savedMealIds = savedDetails.mapNotNull { it.idMeal }.toSet()
                    )
                }
            }
        }
    }

    fun refresh() {
        loadInitialData()
    }

    fun selectCategory(categoryName: String?) {
        categoryJob?.cancel()

        categoryJob = viewModelScope.launch {
            val current = _state.value as? FeedUiState.Success ?: return@launch
            if (categoryName == current.selectedCategory) return@launch

            _state.value = current.copy(isLoading = true, selectedCategory = categoryName)

            try {
                val result = if (categoryName == null) {
                    repository.getHomeFeed()
                } else {
                    repository.getMealsByCategory(categoryName)
                }

                when (result) {
                    is Resource.Success -> {
                        val optimizedMeals = dedupe(result.data)
                        val updated = _state.value as? FeedUiState.Success ?: return@launch
                        _state.value = updated.copy(
                            feedMeals = optimizedMeals,
                            meals = optimizedMeals,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _state.value = FeedUiState.Error(result.message)
                    }
                    is Resource.Loading -> Unit
                }
            } catch (_: Exception) {
                val updated = _state.value as? FeedUiState.Success
                if (updated != null) {
                    _state.value = updated.copy(isLoading = false)
                }
            }
        }
    }

    fun toggleSearchMode() {
        val current = _state.value
        if (current is FeedUiState.Success) {
            val nowSearching = !current.isSearching
            _state.value = current.copy(isSearching = nowSearching)
            if (!nowSearching) {
                // Leaving search: clear query and restore feed
                onSearchQueryChange("")
                val refreshed = _state.value
                if (refreshed is FeedUiState.Success) {
                    _state.value = refreshed.copy(meals = refreshed.feedMeals, isLoading = false)
                }
            }
        }
    }

    fun toggleSave(meal: MealFeedItem) {
        viewModelScope.launch {
            val current = _state.value
            if (current is FeedUiState.Success) {
                val id = meal.idMeal ?: return@launch
                if (current.savedMealIds.contains(id)) {
                    repository.unsaveMeal(id)
                } else {
                    when (val detailResult = repository.getMealDetail(id)) {
                        is Resource.Success -> repository.saveMeal(detailResult.data)
                        else -> Unit
                    }
                }
            }
        }
    }

    fun loadMoreMeals() {
        // Prevent multiple simultaneous load operations
        if (loadMoreJob?.isActive == true) return

        loadMoreJob = viewModelScope.launch {
            val current = _state.value as? FeedUiState.Success ?: return@launch

            // Check conditions
            if (current.isLoading ||
                current.selectedCategory != null ||
                current.isSearching ||
                current.feedMeals.size >= MAX_FEED_ITEMS) {
                return@launch
            }

            _state.value = current.copy(isLoading = true)

            try {
                when (val result = repository.loadMoreMeals(LOAD_MORE_COUNT)) {
                    is Resource.Success -> {
                        val updated = _state.value as? FeedUiState.Success ?: return@launch
                        val combined = updated.feedMeals + result.data
                        val optimizedMeals = dedupe(combined)

                        _state.value = updated.copy(
                            feedMeals = optimizedMeals,
                            meals = optimizedMeals,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        val updated = _state.value as? FeedUiState.Success
                        if (updated != null) {
                            _state.value = updated.copy(isLoading = false)
                        }
                    }
                    is Resource.Loading -> Unit
                }
            } catch (_: Exception) {
                val updated = _state.value as? FeedUiState.Success
                if (updated != null) {
                    _state.value = updated.copy(isLoading = false)
                }
            }
        }
    }
}

