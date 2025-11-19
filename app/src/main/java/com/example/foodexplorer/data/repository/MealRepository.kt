package com.example.foodexplorer.data.repository

import com.example.foodexplorer.data.model.Category
import com.example.foodexplorer.data.model.MealDetail
import com.example.foodexplorer.data.model.MealFeedItem
import com.example.foodexplorer.data.util.Resource
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    suspend fun getHomeFeed(): Resource<List<MealFeedItem>>
    suspend fun loadMoreMeals(count: Int = 6): Resource<List<MealFeedItem>>
    suspend fun getCategories(): Resource<List<Category>>
    suspend fun getMealsByCategory(category: String): Resource<List<MealFeedItem>>
    suspend fun getMealDetail(idMeal: String): Resource<MealDetail>
    suspend fun searchMeals(query: String): Resource<List<MealDetail>>

    // Saved meals
    fun getSavedMeals(): Flow<List<MealDetail>>
    suspend fun saveMeal(meal: MealDetail)
    suspend fun unsaveMeal(mealId: String)
    suspend fun isMealSaved(mealId: String): Boolean
}
