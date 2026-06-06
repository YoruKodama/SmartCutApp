package com.example.smartcutapp.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object Recipes : Screen("recipes")
    object Settings : Screen("settings")
    object Register : Screen("register")
    object CreateRecipe : Screen("create_recipe")
    object BladeSettings : Screen("blade_settings?ingredient={ingredient}") {
        fun createRoute(ingredient: String = "") =
            if (ingredient.isEmpty()) "blade_settings"
            else "blade_settings?ingredient=${java.net.URLEncoder.encode(ingredient, "UTF-8")}"
    }
    object Slices : Screen("slices?ingredient={ingredient}") {
        fun createRoute(ingredient: String = "") =
            if (ingredient.isEmpty()) "slices"
            else "slices?ingredient=${java.net.URLEncoder.encode(ingredient, "UTF-8")}"
    }
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: Int) = "recipe_detail/$recipeId"
    }
}
