package com.example.smartcutapp.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartcutapp.data.remote.api.TokenStorage
import com.example.smartcutapp.presentation.screens.blade.BladeCubeScreen
import com.example.smartcutapp.presentation.screens.blade.SlicesScreen
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
        composable(Screen.CreateRecipe.route) { CreateRecipeScreen(navController) }
        composable(
            route = Screen.BladeSettings.route,
            arguments = listOf(navArgument("ingredient") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStack ->
            val ingredient = backStack.arguments?.getString("ingredient") ?: ""
            BladeCubeScreen(navController, ingredient)
        }
        composable(
            route = Screen.Slices.route,
            arguments = listOf(navArgument("ingredient") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStack ->
            val ingredient = backStack.arguments?.getString("ingredient") ?: ""
            SlicesScreen(navController, ingredient)
        }
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("recipeId") ?: 0
            RecipeDetailScreen(navController, recipeId = id)
        }
    }
}
