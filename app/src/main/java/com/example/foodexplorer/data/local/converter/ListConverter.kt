package com.example.foodexplorer.data.local.converter

import androidx.room.TypeConverter
import com.example.foodexplorer.data.model.Ingredient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListConverter {
    @TypeConverter
    fun fromString(value: String): List<Ingredient> {
        val listType = object : TypeToken<List<Ingredient>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<Ingredient>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}