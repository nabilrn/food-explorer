package com.example.foodexplorer.data.local.converter

import androidx.room.TypeConverter
import com.example.foodexplorer.data.model.Ingredient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListConverter {
    private val gson = Gson()
    private val ingredientListType = TypeToken.getParameterized(
        List::class.java,
        Ingredient::class.java
    ).type

    @TypeConverter
    fun fromString(value: String): List<Ingredient> {
        return gson.fromJson(value, ingredientListType)
    }

    @TypeConverter
    fun fromList(list: List<Ingredient>): String {
        return gson.toJson(list)
    }
}
