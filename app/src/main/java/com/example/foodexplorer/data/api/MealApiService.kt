package com.example.foodexplorer.data.api

import com.example.foodexplorer.BuildConfig
import com.example.foodexplorer.data.model.CategoryResponse
import com.example.foodexplorer.data.model.MealDetailResponse
import com.example.foodexplorer.data.model.MealFeedResponse
import com.example.foodexplorer.data.model.SearchResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {
    @GET("categories.php")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): Response<MealFeedResponse>

    @GET("lookup.php")
    suspend fun getMealDetail(@Query("i") id: String): Response<MealDetailResponse>

    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): Response<SearchResponse>

    @GET("random.php")
    suspend fun getRandomMeal(): Response<MealDetailResponse>

    companion object {
        private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
        private const val TIMEOUT_SECONDS = 30L

        fun create(): MealApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.BASIC
                }
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MealApiService::class.java)
        }
    }
}
