package com.example.foodexplorer.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import com.example.foodexplorer.data.api.MealApiService
import com.example.foodexplorer.data.local.FoodExplorerDatabase
import com.example.foodexplorer.data.repository.MealRepositoryImpl
import com.example.foodexplorer.ui.detail.DetailScreen
import com.example.foodexplorer.ui.detail.DetailViewModel
import com.example.foodexplorer.ui.detail.DetailViewModelFactory
import com.example.foodexplorer.ui.feed.FeedScreen
import com.example.foodexplorer.ui.feed.FeedUiState
import com.example.foodexplorer.ui.feed.FeedViewModel
import com.example.foodexplorer.ui.feed.FeedViewModelFactory
import com.example.foodexplorer.ui.nav.BottomNavItem
import com.example.foodexplorer.ui.nav.Screen
import com.example.foodexplorer.ui.saved.SavedScreen
import com.example.foodexplorer.ui.theme.BottomNavGlass
import com.example.foodexplorer.ui.theme.FoodExplorerTheme
import kotlinx.coroutines.launch

@Composable
fun FoodExplorerApp() {
    FoodExplorerTheme {
        val view = LocalView.current
        val context = LocalContext.current

        // Set status bar to show dark icons (for light background)
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = true
            }
        }

        val repository = remember {
            val db = FoodExplorerDatabase.getDatabase(context)
            MealRepositoryImpl(context, MealApiService.create(), db)
        }
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                // Only show bottom navigation on Feed and Saved screens, hide on detail
                if (currentRoute != null && !currentRoute.startsWith("detail")) {
                    // Modern glass-effect bottom navigation
                    NavigationBar(
                        containerColor = BottomNavGlass,
                        tonalElevation = 0.dp
                    ) {
                        val currentDestination = navBackStackEntry?.destination
                        val items = listOf(BottomNavItem.Feed, BottomNavItem.Saved)
                        items.forEach { screen ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = when (screen.route) {
                                            Screen.Feed.route -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                                            Screen.Saved.route -> if (isSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                                            else -> screen.icon
                                        },
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController,
                startDestination = Screen.Feed.route,
                Modifier.padding(innerPadding)
            ) {
                composable(
                    route = Screen.Feed.route,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    exitTransition = {
                        when (targetState.destination.route) {
                            Screen.Saved.route -> slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                            "detail/{mealId}" -> slideOutVertically(
                                targetOffsetY = { -it / 4 },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(200))
                            else -> fadeOut(animationSpec = tween(300))
                        }
                    },
                    popEnterTransition = {
                        when (initialState.destination.route) {
                            "detail/{mealId}" -> fadeIn(animationSpec = tween(300))
                            else -> slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        }
                    },
                    popExitTransition = {
                        when (targetState.destination.route) {
                            Screen.Saved.route -> slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                            else -> fadeOut(animationSpec = tween(300))
                        }
                    }
                ) {
                    val feedViewModel: FeedViewModel =
                        viewModel(factory = FeedViewModelFactory(repository))
                    val feedState = feedViewModel.state.collectAsState().value
                    FeedScreen(
                        state = feedState,
                        onMealClick = { id -> navController.navigate("detail/$id") },
                        onRefresh = { feedViewModel.refresh() },
                        onToggleSave = { mealId ->
                            val isSaved = (feedState as? FeedUiState.Success)?.savedMealIds?.contains(mealId) ?: false
                            feedViewModel.toggleSave(mealId)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (isSaved) "Meal removed" else "Meal saved"
                                )
                            }
                        },
                        onCategoryClick = { category -> feedViewModel.selectCategory(category) },
                        onSearchMeals = { query -> feedViewModel.searchMeals(query) },
                        onToggleSearchMode = { feedViewModel.toggleSearchMode() },
                        onLoadMore = { feedViewModel.loadMoreMeals() },
                        onShareMeal = { meal ->
                            val shareText = buildString {
                                append("Check out this recipe: ${meal.strMeal}\n\n")
                                append("Discover more delicious recipes in Food Explorer app!")
                            }
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, "Share recipe")
                            context.startActivity(shareIntent)
                        }
                    )
                }
                composable(
                    route = Screen.Saved.route,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    exitTransition = {
                        when (targetState.destination.route) {
                            Screen.Feed.route -> slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                            "detail/{mealId}" -> slideOutVertically(
                                targetOffsetY = { -it / 4 },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(200))
                            else -> fadeOut(animationSpec = tween(300))
                        }
                    },
                    popEnterTransition = {
                        when (initialState.destination.route) {
                            "detail/{mealId}" -> fadeIn(animationSpec = tween(300))
                            else -> slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        }
                    },
                    popExitTransition = {
                        when (targetState.destination.route) {
                            Screen.Feed.route -> slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                            else -> fadeOut(animationSpec = tween(300))
                        }
                    }
                ) {
                    SavedScreen(
                        onMealClick = { id -> navController.navigate("detail/$id") },
                        repository = repository
                    )
                }
                composable(
                    route = "detail/{mealId}",
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(400)
                        ) + fadeIn(animationSpec = tween(400))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(200))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(400)
                        ) + fadeOut(animationSpec = tween(300))
                    }
                ) { backStackEntry ->
                    val mealId = backStackEntry.arguments?.getString("mealId")!!
                    val detailViewModel: DetailViewModel = viewModel(
                        factory = DetailViewModelFactory(repository, mealId)
                    )
                    val isSaved = detailViewModel.isSaved.collectAsState().value
                    DetailScreen(
                        state = detailViewModel.state.collectAsState().value,
                        isSaved = isSaved,
                        onBack = { navController.popBackStack() },
                        onRetry = { detailViewModel.loadMeal(mealId) },
                        onToggleSave = {
                            detailViewModel.toggleSave()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (isSaved) "Meal removed" else "Meal saved"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun FoodExplorerAppPreview() {
    FoodExplorerApp()
}
