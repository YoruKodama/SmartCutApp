package com.example.smartcutapp.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartcutapp.data.remote.api.ApiClient
import com.example.smartcutapp.data.remote.api.TokenStorage
import com.example.smartcutapp.presentation.screens.ai_recipe.AiRecipeScreen
import com.example.smartcutapp.presentation.screens.cooking.CookingScreen
import com.example.smartcutapp.presentation.screens.device.DeviceSetupScreen
import com.example.smartcutapp.presentation.screens.journal.JournalScreen
import com.example.smartcutapp.presentation.screens.product_scan.ProductScanScreen
import com.example.smartcutapp.presentation.screens.create_recipe.CreateRecipeScreen
import com.example.smartcutapp.presentation.screens.login.LoginScreen
import com.example.smartcutapp.presentation.screens.main.MainScreen
import com.example.smartcutapp.presentation.screens.recipe_detail.RecipeDetailScreen
import com.example.smartcutapp.presentation.screens.recipes.RecipesScreen
import com.example.smartcutapp.presentation.screens.register.RegisterScreen
import com.example.smartcutapp.presentation.screens.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController, padding: PaddingValues) {
    val startDestination = if (TokenStorage.token.isNotEmpty()) Screen.Main.route else Screen.Login.route

    // Бэк отклонил токен — возвращаем на вход
    LaunchedEffect(Unit) {
        ApiClient.sessionExpired.collect {
            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(padding)
    ) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Main.route) { MainScreen(navController) }
        composable(Screen.Recipes.route) { RecipesScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(
            route = Screen.CreateRecipe.route,
            arguments = listOf(navArgument("recipeId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStack ->
            val id = backStack.arguments?.getInt("recipeId") ?: -1
            CreateRecipeScreen(navController, editRecipeId = id.takeIf { it >= 0 })
        }
        composable(Screen.Journal.route) { JournalScreen(navController) }
        composable(Screen.DeviceSetup.route) { DeviceSetupScreen(navController) }
        composable(
            route = Screen.Cooking.route,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.IntType },
                navArgument("servings") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStack ->
            CookingScreen(
                navController,
                recipeId = backStack.arguments?.getInt("recipeId") ?: 0,
                servings = backStack.arguments?.getInt("servings") ?: 0
            )
        }
        composable(Screen.AiRecipe.route) { AiRecipeScreen(navController) }
        composable(Screen.ProductScan.route) { ProductScanScreen(navController) }
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("recipeId") ?: 0
            RecipeDetailScreen(navController, recipeId = id)
        }
    }
}
