package com.example.foodexplorer.ui.saved

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodexplorer.data.repository.MealRepository
import com.example.foodexplorer.ui.components.TopBar

@Composable
fun SavedScreen(
    repository: MealRepository,
    onMealClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SavedViewModel = viewModel(factory = SavedViewModelFactory(repository))
    val savedMeals by viewModel.savedMeals.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(title = "Saved Meals")

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(savedMeals) { meal ->
                SavedMealItem(
                    meal = meal,
                    onMealClick = onMealClick,
                    onUnsave = { mealId ->
                        viewModel.unsaveMeal(mealId)
                    }
                )
            }
        }
    }
}

