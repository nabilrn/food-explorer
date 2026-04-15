package com.example.foodexplorer.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.foodexplorer.data.api.MealApiService
import com.example.foodexplorer.data.local.FoodExplorerDatabase
import com.example.foodexplorer.data.repository.MealRepositoryImpl
import com.example.foodexplorer.ui.detail.DetailScreen
import com.example.foodexplorer.ui.detail.DetailViewModel
import com.example.foodexplorer.ui.feed.FeedScreen
import com.example.foodexplorer.ui.feed.FeedUiState
import com.example.foodexplorer.ui.feed.FeedViewModel
import com.example.foodexplorer.ui.nav.BottomNavItem
import com.example.foodexplorer.ui.nav.Screen
import com.example.foodexplorer.ui.saved.SavedScreen
import com.example.foodexplorer.ui.splash.SplashScreen
import com.example.foodexplorer.ui.theme.FoodExplorerTheme
import kotlinx.coroutines.launch

@Composable
fun FoodExplorerApp() {
    FoodExplorerTheme {
        val view = LocalView.current
        val context = LocalContext.current
        val darkTheme = isSystemInDarkTheme()

        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
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
        val feedViewModel: FeedViewModel = viewModel(factory = ViewModelFactory(repository))

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (currentRoute != null &&
                    !currentRoute.startsWith("detail") &&
                    currentRoute != Screen.Splash.route
                ) {
                    Surface(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))) {
                        NavigationBar(
                            modifier = Modifier.navigationBarsPadding(),
                            containerColor = MaterialTheme.colorScheme.surface,
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
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(
                    route = Screen.Splash.route,
                    enterTransition = { fadeIn(animationSpec = tween(300)) + slideInVertically(tween(300)) { 30 } },
                    exitTransition = { fadeOut(animationSpec = tween(200)) }
                ) {
                    val feedState = feedViewModel.state.collectAsState().value
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(Screen.Feed.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        feedState = feedState
                    )
                }

                composable(
                    route = Screen.Feed.route,
                    enterTransition = { fadeIn(animationSpec = tween(300)) + slideInVertically(tween(300)) { 30 } },
                    exitTransition = { fadeOut(animationSpec = tween(200)) }
                ) {
                    val feedState = feedViewModel.state.collectAsState().value
                    val searchQuery = feedViewModel.searchQuery.collectAsState().value
                    FeedScreen(
                        state = feedState,
                        searchQuery = searchQuery,
                        onMealClick = { id -> navController.navigate("detail/$id") },
                        onRefresh = { feedViewModel.refresh() },
                        onToggleSave = { meal ->
                            val isSaved = (feedState as? FeedUiState.Success)
                                ?.savedMealIds
                                ?.contains(meal.idMeal ?: "") == true
                            feedViewModel.toggleSave(meal)
                            scope.launch {
                                snackbarHostState.showSnackbar(if (isSaved) "Meal removed" else "Meal saved")
                            }
                        },
                        onCategoryClick = { category -> feedViewModel.selectCategory(category) },
                        onSearchQueryChange = { q -> feedViewModel.onSearchQueryChange(q) },
                        onToggleSearchMode = { feedViewModel.toggleSearchMode() },
                        onLoadMore = { feedViewModel.loadMoreMeals() }
                    )
                }

                composable(
                    route = Screen.Saved.route,
                    enterTransition = { fadeIn(animationSpec = tween(300)) + slideInVertically(tween(300)) { 30 } },
                    exitTransition = { fadeOut(animationSpec = tween(200)) }
                ) {
                    SavedScreen(
                        onMealClick = { id -> navController.navigate("detail/$id") },
                        repository = repository
                    )
                }

                composable(
                    route = "detail/{mealId}",
                    enterTransition = { fadeIn(animationSpec = tween(300)) + slideInVertically(tween(300)) { 30 } },
                    exitTransition = { fadeOut(animationSpec = tween(200)) }
                ) { backStackEntry ->
                    val mealId = backStackEntry.arguments?.getString("mealId")!!
                    val detailViewModel: DetailViewModel = viewModel(factory = ViewModelFactory(repository))
                    val isSaved = detailViewModel.isSaved.collectAsState().value
                    DetailScreen(
                        state = detailViewModel.state.collectAsState().value,
                        isSaved = isSaved,
                        onBack = { navController.popBackStack() },
                        onRetry = { detailViewModel.loadMeal(mealId) },
                        onToggleSave = {
                            detailViewModel.toggleSave()
                            scope.launch {
                                snackbarHostState.showSnackbar(if (isSaved) "Meal removed" else "Meal saved")
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
