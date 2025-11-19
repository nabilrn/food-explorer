package com.example.foodexplorer.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodexplorer.data.model.MealDetail
import com.example.foodexplorer.data.repository.MealRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(private val repository: MealRepository) : ViewModel() {

    val savedMeals: StateFlow<List<MealDetail>> = repository.getSavedMeals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun unsaveMeal(mealId: String) {
        viewModelScope.launch {
            repository.unsaveMeal(mealId)
        }
    }
}