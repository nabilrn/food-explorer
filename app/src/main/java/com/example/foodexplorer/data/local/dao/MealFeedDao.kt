package com.example.foodexplorer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.foodexplorer.data.local.entity.MealFeedItemEntity

@Dao
interface MealFeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<MealFeedItemEntity>)

    @Query("SELECT * FROM meal_feed")
    suspend fun getAll(): List<MealFeedItemEntity>

    @Query("SELECT * FROM meal_feed LIMIT :limit")
    suspend fun getLimit(limit: Int): List<MealFeedItemEntity>

    @Query("DELETE FROM meal_feed")
    suspend fun deleteAll()
}