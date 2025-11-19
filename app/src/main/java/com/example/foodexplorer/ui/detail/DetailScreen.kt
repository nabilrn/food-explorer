package com.example.foodexplorer.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.foodexplorer.ui.components.TopBar

@Composable
fun DetailScreen(
    state: DetailUiState,
    isSaved: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        when (state) {
            DetailUiState.Loading -> {
                TopBar(title = "Detail", onNavigationClick = onBack)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }

            is DetailUiState.Error -> {
                TopBar(title = "Detail", onNavigationClick = onBack)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onRetry) {
                        Text(text = "Retry")
                    }
                }
            }

            is DetailUiState.Success -> {
                TopBar(
                    title = state.meal.strMeal ?: "Detail",
                    onNavigationClick = onBack
                )
                val context = LocalContext.current

                // Share function
                val shareMeal = {
                    val shareText = buildString {
                        append("Check out this recipe: ${state.meal.strMeal}\n\n")
                        if (!state.meal.strCategory.isNullOrEmpty()) {
                            append("Category: ${state.meal.strCategory}\n")
                        }
                        if (!state.meal.strArea.isNullOrEmpty()) {
                            append("Origin: ${state.meal.strArea}\n")
                        }
                        if (!state.meal.strYoutube.isNullOrEmpty()) {
                            append("\nWatch video: ${state.meal.strYoutube}")
                        }
                    }

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share recipe")
                    context.startActivity(shareIntent)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    DetailHeader(
                        meal = state.meal,
                        isSaved = isSaved,
                        onToggleSave = onToggleSave,
                        onShare = shareMeal
                    )

                Spacer(modifier = Modifier.height(24.dp))

                DetailIngredientsSection(meal = state.meal)

                Spacer(modifier = Modifier.height(24.dp))

                DetailInstructionsSection(instructions = state.meal.strInstructions)

                // YouTube button - modern, minimal design
                if (!state.meal.strYoutube.isNullOrEmpty()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.meal.strYoutube))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Watch Tutorial on YouTube",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

