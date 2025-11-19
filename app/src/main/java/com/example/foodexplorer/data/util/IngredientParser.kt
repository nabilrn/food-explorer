package com.example.foodexplorer.data.util

import com.example.foodexplorer.data.model.Ingredient
import com.example.foodexplorer.data.model.MealDetailRaw

object IngredientParser {
    fun parse(raw: MealDetailRaw): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        val ingredientFields = listOf(
            raw.strIngredient1 to raw.strMeasure1,
            raw.strIngredient2 to raw.strMeasure2,
            raw.strIngredient3 to raw.strMeasure3,
            raw.strIngredient4 to raw.strMeasure4,
            raw.strIngredient5 to raw.strMeasure5,
            raw.strIngredient6 to raw.strMeasure6,
            raw.strIngredient7 to raw.strMeasure7,
            raw.strIngredient8 to raw.strMeasure8,
            raw.strIngredient9 to raw.strMeasure9,
            raw.strIngredient10 to raw.strMeasure10,
            raw.strIngredient11 to raw.strMeasure11,
            raw.strIngredient12 to raw.strMeasure12,
            raw.strIngredient13 to raw.strMeasure13,
            raw.strIngredient14 to raw.strMeasure14,
            raw.strIngredient15 to raw.strMeasure15,
            raw.strIngredient16 to raw.strMeasure16,
            raw.strIngredient17 to raw.strMeasure17,
            raw.strIngredient18 to raw.strMeasure18,
            raw.strIngredient19 to raw.strMeasure19,
            raw.strIngredient20 to raw.strMeasure20
        )

        ingredientFields.forEach { (ingredient, measure) ->
            val name = ingredient?.trim().orEmpty()
            val qty = measure?.trim().orEmpty()
            if (name.isNotEmpty()) {
                ingredients.add(Ingredient(name = name, measure = qty))
            }
        }
        return ingredients
    }
}

