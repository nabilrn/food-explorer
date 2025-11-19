package com.example.foodexplorer.data.local.mapper

import com.example.foodexplorer.data.local.entity.MealDetailEntity
import com.example.foodexplorer.data.model.MealDetail

fun MealDetail.toEntity() = MealDetailEntity(
    idMeal = idMeal ?: "",
    strMeal = strMeal,
    strCategory = strCategory,
    strArea = strArea,
    strInstructions = strInstructions,
    strMealThumb = strMealThumb,
    strTags = strTags,
    strYoutube = strYoutube,
    ingredients = ingredients
)

fun MealDetailEntity.toModel() = MealDetail(
    idMeal = idMeal,
    strMeal = strMeal,
    strCategory = strCategory,
    strArea = strArea,
    strInstructions = strInstructions,
    strMealThumb = strMealThumb,
    strTags = strTags,
    strYoutube = strYoutube,
    ingredients = ingredients
)