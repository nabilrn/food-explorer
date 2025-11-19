package com.example.foodexplorer.data.repository

import android.content.Context
import com.example.foodexplorer.data.api.MealApiService
import com.example.foodexplorer.data.local.FoodExplorerDatabase
import com.example.foodexplorer.data.local.mapper.toEntity
import com.example.foodexplorer.data.local.mapper.toModel
import com.example.foodexplorer.data.model.Category
import com.example.foodexplorer.data.model.MealDetail
import com.example.foodexplorer.data.model.MealFeedItem
import com.example.foodexplorer.data.util.IngredientParser
import com.example.foodexplorer.data.util.NetworkUtils
import com.example.foodexplorer.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class MealRepositoryImpl(
    private val context: Context,
    private val apiService: MealApiService,
    db: FoodExplorerDatabase
) : MealRepository {

    private val mealFeedDao = db.mealFeedDao()
    private val mealDetailDao = db.mealDetailDao()

    override suspend fun getHomeFeed(): Resource<List<MealFeedItem>> = withContext(Dispatchers.IO) {
        // Check if network is available
        if (!NetworkUtils.isNetworkAvailable(context)) {
            // No internet - load 10 items from cache
            val cachedMeals = mealFeedDao.getLimit(10).map { it.toModel() }
            return@withContext if (cachedMeals.isNotEmpty()) {
                Resource.Success(cachedMeals)
            } else {
                Resource.Error("No internet connection and no cached data available")
            }
        }

        // Internet available - fetch from API
        try {
            val categoriesResponse = apiService.getCategories()
            if (!categoriesResponse.isSuccessful) {
                // If API fails, try loading 10 items from cache
                val cachedMeals = mealFeedDao.getLimit(10).map { it.toModel() }
                return@withContext if (cachedMeals.isNotEmpty()) {
                    Resource.Success(cachedMeals)
                } else {
                    Resource.Error("Failed to load categories: ${categoriesResponse.message()}")
                }
            }

            val categories = categoriesResponse.body()?.categories
                ?.take(5)
                ?: run {
                    val cachedMeals = mealFeedDao.getLimit(10).map { it.toModel() }
                    return@withContext if (cachedMeals.isNotEmpty()) {
                        Resource.Success(cachedMeals)
                    } else {
                        Resource.Error("Categories not available")
                    }
                }

            val allMeals = mutableListOf<MealFeedItem>()
            categories.forEach { category ->
                val name = category.strCategory ?: return@forEach
                try {
                    val mealsResponse = apiService.getMealsByCategory(name)
                    if (mealsResponse.isSuccessful) {
                        mealsResponse.body()?.meals?.let { allMeals.addAll(it) }
                    }
                } catch (e: Exception) {
                    // Continue with other categories even if one fails
                }
            }

            if (allMeals.isNotEmpty()) {
                allMeals.shuffle()
                // Save to cache
                mealFeedDao.deleteAll()
                mealFeedDao.insertAll(allMeals.map { it.toEntity() })
                Resource.Success(allMeals)
            } else {
                // If no meals from API, load 10 items from cache
                val cachedMeals = mealFeedDao.getLimit(10).map { it.toModel() }
                if (cachedMeals.isNotEmpty()) {
                    Resource.Success(cachedMeals)
                } else {
                    Resource.Error("No meals available")
                }
            }
        } catch (e: Exception) {
            // On any error, try loading 10 items from cache
            val cachedMeals = mealFeedDao.getLimit(10).map { it.toModel() }
            if (cachedMeals.isNotEmpty()) {
                Resource.Success(cachedMeals)
            } else {
                Resource.Error("Couldn't fetch feed. Please check your internet connection.", e)
            }
        }
    }

    override suspend fun getCategories(): Resource<List<Category>> = safeApiCall {
        val response = apiService.getCategories()
        if (response.isSuccessful) {
            val categories = response.body()?.categories
            Resource.Success(categories ?: emptyList())
        } else {
            Resource.Error("Failed to load categories: ${response.message()}")
        }
    }

    override suspend fun getMealsByCategory(category: String): Resource<List<MealFeedItem>> = safeApiCall {
        val response = apiService.getMealsByCategory(category)
        if (response.isSuccessful) {
            Resource.Success(response.body()?.meals ?: emptyList())
        } else {
            Resource.Error("Failed to load meals: ${response.message()}")
        }
    }

    override suspend fun getMealDetail(idMeal: String): Resource<MealDetail> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            val cachedMeal = mealDetailDao.getMealById(idMeal)?.toModel()
            return@withContext if (cachedMeal != null) {
                Resource.Success(cachedMeal)
            } else {
                Resource.Error("No internet connection and meal not cached")
            }
        }

        // Internet available - fetch from API
        try {
            val response = apiService.getMealDetail(idMeal)
            if (response.isSuccessful) {
                val meal = response.body()?.meals?.firstOrNull()
                    ?: run {
                        val cachedMeal = mealDetailDao.getMealById(idMeal)?.toModel()
                        return@withContext if (cachedMeal != null) {
                            Resource.Success(cachedMeal)
                        } else {
                            Resource.Error("Meal not found")
                        }
                    }

                val ingredients = IngredientParser.parse(meal)
                val mealDetail = MealDetail(
                    idMeal = meal.idMeal,
                    strMeal = meal.strMeal,
                    strCategory = meal.strCategory,
                    strArea = meal.strArea,
                    strInstructions = meal.strInstructions,
                    strMealThumb = meal.strMealThumb,
                    strTags = meal.strTags,
                    strYoutube = meal.strYoutube,
                    ingredients = ingredients
                )

                // Save to cache
                val existingMeal = mealDetailDao.getMealById(idMeal)
                val isSaved = existingMeal?.isSaved ?: false
                val savedAt = existingMeal?.savedAt ?: 0L

                mealDetailDao.insert(mealDetail.toEntity().copy(
                    isSaved = isSaved,
                    savedAt = savedAt
                ))

                Resource.Success(mealDetail)
            } else {
                // If API fails, try loading from cache
                val cachedMeal = mealDetailDao.getMealById(idMeal)?.toModel()
                if (cachedMeal != null) {
                    Resource.Success(cachedMeal)
                } else {
                    Resource.Error("Failed to load meal detail: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            // On any error, try loading from cache
            val cachedMeal = mealDetailDao.getMealById(idMeal)?.toModel()
            if (cachedMeal != null) {
                Resource.Success(cachedMeal)
            } else {
                Resource.Error("Couldn't fetch meal detail. Please check your internet connection.", e)
            }
        }
    }

    override fun getSavedMeals(): Flow<List<MealDetail>> {
        return mealDetailDao.getAllSavedMeals().map { entities ->
            entities.map { entity -> entity.toModel() }
        }
    }

    override suspend fun saveMeal(meal: MealDetail) {
        val entity = meal.toEntity().copy(isSaved = true, savedAt = System.currentTimeMillis())
        mealDetailDao.insert(entity)
    }

    override suspend fun unsaveMeal(mealId: String) {
        mealDetailDao.unsaveMeal(mealId)
    }

    override suspend fun isMealSaved(mealId: String): Boolean {
        return mealDetailDao.isMealSaved(mealId) ?: false
    }

    override suspend fun searchMeals(query: String): Resource<List<MealDetail>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext Resource.Success(emptyList())
        }

        try {
            val response = apiService.searchMeals(query)
            if (response.isSuccessful) {
                val meals = response.body()?.meals?.map { mealRaw ->
                    val ingredients = IngredientParser.parse(mealRaw)
                    MealDetail(
                        idMeal = mealRaw.idMeal,
                        strMeal = mealRaw.strMeal,
                        strCategory = mealRaw.strCategory,
                        strArea = mealRaw.strArea,
                        strInstructions = mealRaw.strInstructions,
                        strMealThumb = mealRaw.strMealThumb,
                        strTags = mealRaw.strTags,
                        strYoutube = mealRaw.strYoutube,
                        ingredients = ingredients
                    )
                } ?: emptyList()
                Resource.Success(meals)
            } else {
                Resource.Error("Search failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Couldn't search meals. Please check your internet connection.", e)
        }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Resource<T>): Resource<T> {
        return try {
            withContext(Dispatchers.IO) { apiCall() }
        } catch (e: SocketTimeoutException) {
            Resource.Error("Request timed out", e)
        } catch (e: IOException) {
            Resource.Error("Network error", e)
        } catch (e: HttpException) {
            Resource.Error("HTTP error ${e.code()}", e)
        } catch (e: Exception) {
            Resource.Error("Unexpected error", e)
        }
    }
}