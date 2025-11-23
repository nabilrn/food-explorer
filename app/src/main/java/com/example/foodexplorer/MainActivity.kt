package com.example.foodexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.foodexplorer.ui.FoodExplorerApp
import com.example.foodexplorer.ui.theme.FoodExplorerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodExplorerTheme {
                FoodExplorerApp()
            }
        }
    }
}
