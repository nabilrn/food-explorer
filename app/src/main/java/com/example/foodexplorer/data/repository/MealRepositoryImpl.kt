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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    private val mealDetailDao = db.mealDetailDao()

    override suspend fun getHomeFeed(): Resource<List<MealFeedItem>> = withContext(Dispatchers.IO) {
        // Check if network is available
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Resource.Error("no network see your saved meal")
        }
        try {
            // Launch 15 parallel random fetches for initial load
            val deferred = (1..15).map {
                async {
                    try {
                        val resp = apiService.getRandomMeal()
                        if (resp.isSuccessful) {
                            resp.body()?.meals?.firstOrNull()?.let { m ->
                                MealFeedItem(
                                    idMeal = m.idMeal,
                                    strMeal = m.strMeal,
                                    strMealThumb = m.strMealThumb,
                                    strCategory = m.strCategory,
                                    strArea = m.strArea
                                )
                            }
                        } else null
                    } catch (_: Exception) { null }
                }
            }
            val allMeals = deferred.awaitAll().filterNotNull()

            if (allMeals.isNotEmpty()) {
                Resource.Success(allMeals)
            } else {
                Resource.Error("No meals available")
            }
        } catch (e: Exception) {
            Resource.Error("no network see your saved meal", e)
        }
    }

    override suspend fun loadMoreMeals(count: Int): Resource<List<MealFeedItem>> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Resource.Error("No internet connection")
        }

        try {
            val deferred = (1..count).map {
                async {
                    try {
                        val resp = apiService.getRandomMeal()
                        if (resp.isSuccessful) {
                            resp.body()?.meals?.firstOrNull()?.let { m ->
                                MealFeedItem(
                                    idMeal = m.idMeal,
                                    strMeal = m.strMeal,
                                    strMealThumb = m.strMealThumb,
                                    strCategory = m.strCategory,
                                    strArea = m.strArea
                                )
                            }
                        } else null
                    } catch (_: Exception) { null }
                }
            }
            val newMeals = deferred.awaitAll().filterNotNull()

            if (newMeals.isNotEmpty()) {
                Resource.Success(newMeals)
            } else {
                Resource.Error("Failed to load more meals")
            }
        } catch (e: Exception) {
            Resource.Error("Couldn't load more meals. Please check your internet connection.", e)
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
            val meals = response.body()?.meals?.map { it.copy(strCategory = category) } ?: emptyList()
            Resource.Success(meals)
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
            Resource.Error("No Network, See Your Saved Meal", e)
        } catch (e: HttpException) {
            Resource.Error("HTTP error ${e.code()}", e)
        } catch (e: Exception) {
            Resource.Error("Unexpected error", e)
        }
    }
}
