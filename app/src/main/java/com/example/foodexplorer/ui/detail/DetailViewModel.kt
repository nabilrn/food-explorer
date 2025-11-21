package com.example.foodexplorer.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodexplorer.data.model.MealDetail
import com.example.foodexplorer.data.repository.MealRepository
import com.example.foodexplorer.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val meal: MealDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    private val repository: MealRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state: MutableStateFlow<DetailUiState> = MutableStateFlow(DetailUiState.Loading)
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    private val _isSaved: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private var currentMeal: MealDetail? = null

    init {
        val mealId: String? = savedStateHandle[MEAL_ID_KEY]
        if (mealId != null) {
            loadMeal(mealId)
            checkIfSaved(mealId)
        } else {
            _state.value = DetailUiState.Error("Missing meal id")
        }
    }

    fun loadMeal(mealId: String) {
        viewModelScope.launch {
            _state.value = DetailUiState.Loading
            when (val result = repository.getMealDetail(mealId)) {
                is Resource.Success -> {
                    currentMeal = result.data
                    _state.value = DetailUiState.Success(result.data)
                    checkIfSaved(mealId)
                }
                is Resource.Error -> _state.value = DetailUiState.Error(result.message)
                Resource.Loading -> Unit
            }
        }
    }

    private fun checkIfSaved(mealId: String) {
        viewModelScope.launch {
            _isSaved.value = repository.isMealSaved(mealId)
        }
    }

    fun toggleSave() {
        viewModelScope.launch {
            val meal = currentMeal ?: return@launch
            val mealId = meal.idMeal ?: return@launch
            if (_isSaved.value) {
                repository.unsaveMeal(mealId)
                _isSaved.value = false
            } else {
                repository.saveMeal(meal)
                _isSaved.value = true
            }
        }
    }

    companion object {
        const val MEAL_ID_KEY = "mealId"
    }
}


