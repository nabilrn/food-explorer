package com.example.foodexplorer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.foodexplorer.data.local.converter.ListConverter
import com.example.foodexplorer.data.model.Ingredient

@Entity(tableName = "meal_detail")
@TypeConverters(ListConverter::class)
data class MealDetailEntity(
    @PrimaryKey
    val idMeal: String,
    val strMeal: String?,
    val strCategory: String?,
    val strArea: String?,
    val strInstructions: String?,
    val strMealThumb: String?,
    val strTags: String?,
    val strYoutube: String?,
    val ingredients: List<Ingredient>,
    val isSaved: Boolean = false,
    val savedAt: Long = 0L
)