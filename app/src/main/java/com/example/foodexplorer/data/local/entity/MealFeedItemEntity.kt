package com.example.foodexplorer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_feed")
data class MealFeedItemEntity(
    @PrimaryKey
    val idMeal: String,
    val strMeal: String?,
    val strMealThumb: String?,
    val strCategory: String? = null,
    val strArea: String? = null
)