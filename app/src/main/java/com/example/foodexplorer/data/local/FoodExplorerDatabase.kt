package com.example.foodexplorer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.foodexplorer.data.local.converter.ListConverter
import com.example.foodexplorer.data.local.dao.MealDetailDao
import com.example.foodexplorer.data.local.dao.MealFeedDao
import com.example.foodexplorer.data.local.entity.MealDetailEntity
import com.example.foodexplorer.data.local.entity.MealFeedItemEntity

@Database(
    entities = [MealFeedItemEntity::class, MealDetailEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(ListConverter::class)
abstract class FoodExplorerDatabase : RoomDatabase() {

    abstract fun mealFeedDao(): MealFeedDao
    abstract fun mealDetailDao(): MealDetailDao

    companion object {
        @Volatile
        private var INSTANCE: FoodExplorerDatabase? = null

        fun getDatabase(context: Context): FoodExplorerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodExplorerDatabase::class.java,
                    "food_explorer_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}