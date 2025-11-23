package com.example.foodexplorer.di

import android.content.Context
import androidx.room.Room
import com.example.foodexplorer.data.api.MealApiService
import com.example.foodexplorer.data.local.FoodExplorerDatabase
import com.example.foodexplorer.data.repository.MealRepository
import com.example.foodexplorer.data.repository.MealRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMealApiService(retrofit: Retrofit): MealApiService {
        return retrofit.create(MealApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFoodExplorerDatabase(@ApplicationContext context: Context): FoodExplorerDatabase {
        return Room.databaseBuilder(
            context,
            FoodExplorerDatabase::class.java,
            "food_explorer.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMealRepository(
        @ApplicationContext context: Context,
        apiService: MealApiService,
        db: FoodExplorerDatabase
    ): MealRepository {
        return MealRepositoryImpl(context, apiService, db)
    }
}
