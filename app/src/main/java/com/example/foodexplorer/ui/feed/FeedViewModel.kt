package com.example.foodexplorer.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodexplorer.data.model.Category
import com.example.foodexplorer.data.model.MealFeedItem
import com.example.foodexplorer.data.repository.MealRepository
import com.example.foodexplorer.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FeedUiState {
    object Loading : FeedUiState
    data class Success(
        val categories: List<Category>,
        val meals: List<MealFeedItem>,
        val savedMealIds: Set<String> = emptySet(),
        val selectedCategory: String? = null,
        val allMeals: List<MealFeedItem> = emptyList(),
        val isSearching: Boolean = false,
        val searchQuery: String = "",
        val isLoadingCategory: Boolean = false,
        val isLoadingMore: Boolean = false
    ) : FeedUiState

    data class Error(val message: String) : FeedUiState
}

class FeedViewModel(private val repository: MealRepository) : ViewModel() {

    private val _state: MutableStateFlow<FeedUiState> = MutableStateFlow(FeedUiState.Loading)
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    init {
        refresh()
        observeSavedMeals()
    }

    private fun observeSavedMeals() {
        viewModelScope.launch {
            repository.getSavedMeals().collect { savedMeals ->
                val currentState = _state.value
                if (currentState is FeedUiState.Success) {
                    _state.value = currentState.copy(
                        savedMealIds = savedMeals.mapNotNull { it.idMeal }.toSet()
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = FeedUiState.Loading

            val categoriesResult = repository.getCategories()
            val mealsResult = repository.getHomeFeed()

            if (categoriesResult is Resource.Success && mealsResult is Resource.Success) {
                _state.value = FeedUiState.Success(
                    categories = categoriesResult.data,
                    meals = mealsResult.data,
                    allMeals = mealsResult.data,
                    savedMealIds = emptySet(), // Will be updated by observeSavedMeals
                    selectedCategory = null,
                    isSearching = false,
                    searchQuery = "",
                    isLoadingCategory = false
                )
            } else {
                val errorMessage = (categoriesResult as? Resource.Error)?.message
                    ?: (mealsResult as? Resource.Error)?.message
                    ?: "Unknown error"
                _state.value = FeedUiState.Error(errorMessage)
            }
        }
    }

    fun selectCategory(categoryName: String?) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is FeedUiState.Success) {
                if (categoryName == null || categoryName == currentState.selectedCategory) {
                    // Deselect or show all
                    _state.value = currentState.copy(
                        selectedCategory = null,
                        meals = currentState.allMeals,
                        isLoadingCategory = false
                    )
                } else {
                    // Show loading while fetching category meals
                    _state.value = currentState.copy(
                        selectedCategory = categoryName,
                        isLoadingCategory = true
                    )

                    // Fetch meals for selected category
                    when (val result = repository.getMealsByCategory(categoryName)) {
                        is Resource.Success -> {
                            val newState = _state.value
                            if (newState is FeedUiState.Success) {
                                _state.value = newState.copy(
                                    meals = result.data,
                                    selectedCategory = categoryName,
                                    isLoadingCategory = false
                                )
                            }
                        }
                        is Resource.Error -> {
                            // If error, show all meals
                            _state.value = currentState.copy(
                                selectedCategory = null,
                                meals = currentState.allMeals,
                                isLoadingCategory = false
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    fun searchMeals(query: String) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is FeedUiState.Success) {
                _state.value = currentState.copy(searchQuery = query)

                if (query.isBlank()) {
                    // If query is empty, show all meals or filtered by category
                    _state.value = currentState.copy(
                        meals = if (currentState.selectedCategory != null) {
                            currentState.meals
                        } else {
                            currentState.allMeals
                        },
                        searchQuery = ""
                    )
                    return@launch
                }

                // Search from API
                when (val result = repository.searchMeals(query)) {
                    is Resource.Success -> {
                        // Convert MealDetail to MealFeedItem for display
                        val searchResults = result.data.map { mealDetail ->
                            MealFeedItem(
                                idMeal = mealDetail.idMeal,
                                strMeal = mealDetail.strMeal,
                                strMealThumb = mealDetail.strMealThumb,
                                strCategory = mealDetail.strCategory,
                                strArea = mealDetail.strArea
                            )
                        }
                        val newState = _state.value
                        if (newState is FeedUiState.Success) {
                            _state.value = newState.copy(
                                meals = searchResults,
                                searchQuery = query
                            )
                        }
                    }
                    is Resource.Error -> {
                        // Keep current state on error
                    }
                    else -> Unit
                }
            }
        }
    }

    fun toggleSearchMode() {
        val currentState = _state.value
        if (currentState is FeedUiState.Success) {
            if (currentState.isSearching) {
                // Exit search mode - restore original meals
                _state.value = currentState.copy(
                    isSearching = false,
                    searchQuery = "",
                    meals = if (currentState.selectedCategory != null) {
                        currentState.meals
                    } else {
                        currentState.allMeals
                    }
                )
            } else {
                // Enter search mode
                _state.value = currentState.copy(isSearching = true)
            }
        }
    }

    fun toggleSave(mealId: String) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is FeedUiState.Success) {
                if (currentState.savedMealIds.contains(mealId)) {
                    repository.unsaveMeal(mealId)
                } else {
                    // Fetch meal detail and save it
                    when (val result = repository.getMealDetail(mealId)) {
                        is Resource.Success -> repository.saveMeal(result.data)
                        else -> Unit // Handle error if needed
                    }
                }
            }
        }
    }

    fun loadMoreMeals() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is FeedUiState.Success &&
                !currentState.isLoadingMore &&
                currentState.selectedCategory == null &&
                !currentState.isSearching) {

                _state.value = currentState.copy(isLoadingMore = true)

                when (val result = repository.loadMoreMeals(6)) {
                    is Resource.Success -> {
                        val updatedMeals = currentState.meals + result.data
                        val updatedAllMeals = currentState.allMeals + result.data
                        _state.value = currentState.copy(
                            meals = updatedMeals,
                            allMeals = updatedAllMeals,
                            isLoadingMore = false
                        )
                    }
                    is Resource.Error -> {
                        _state.value = currentState.copy(isLoadingMore = false)
                    }
                    else -> {
                        _state.value = currentState.copy(isLoadingMore = false)
                    }
                }
            }
        }
    }
}

class FeedViewModelFactory(private val repository: MealRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            return FeedViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}