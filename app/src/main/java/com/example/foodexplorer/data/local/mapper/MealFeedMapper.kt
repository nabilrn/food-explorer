package com.example.foodexplorer.data.local.mapper

import com.example.foodexplorer.data.local.entity.MealFeedItemEntity
import com.example.foodexplorer.data.model.MealFeedItem

fun MealFeedItem.toEntity() = MealFeedItemEntity(
    idMeal = idMeal ?: "",
    strMeal = strMeal,
    strMealThumb = strMealThumb,
    strCategory = strCategory,
    strArea = strArea
)

fun MealFeedItemEntity.toModel() = MealFeedItem(
    idMeal = idMeal,
    strMeal = strMeal,
    strMealThumb = strMealThumb,
    strCategory = strCategory,
    strArea = strArea
)