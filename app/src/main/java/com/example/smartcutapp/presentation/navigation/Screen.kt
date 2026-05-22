package com.example.smartcutapp.presentation.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Recipes : Screen("recipes")
    object Settings : Screen("settings")
    object Register : Screen("register")
    object BladeSettings : Screen("blade_settings")
    object Slices : Screen("slices")
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: Int) = "recipe_detail/$recipeId"
    }
}