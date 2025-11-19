package com.example.foodexplorer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.foodexplorer.data.local.entity.MealDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: MealDetailEntity)

    @Query("SELECT * FROM meal_detail WHERE idMeal = :id")
    suspend fun getMealById(id: String): MealDetailEntity?

    @Query("SELECT * FROM meal_detail WHERE isSaved = 1 ORDER BY savedAt DESC")
    fun getAllSavedMeals(): Flow<List<MealDetailEntity>>

    @Query("UPDATE meal_detail SET isSaved = 1, savedAt = :timestamp WHERE idMeal = :id")
    suspend fun saveMeal(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE meal_detail SET isSaved = 0 WHERE idMeal = :id")
    suspend fun unsaveMeal(id: String)

    @Query("SELECT isSaved FROM meal_detail WHERE idMeal = :id")
    suspend fun isMealSaved(id: String): Boolean?

    @Delete
    suspend fun delete(meal: MealDetailEntity)
}