package com.example.foodexplorer.data.model

fun MealDetail.toMealFeedItem() = MealFeedItem(
    idMeal = idMeal,
    strMeal = strMeal,
    strMealThumb = strMealThumb
)