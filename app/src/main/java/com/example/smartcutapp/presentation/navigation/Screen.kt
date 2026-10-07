package com.example.smartcutapp.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object Recipes : Screen("recipes")
    object Settings : Screen("settings")
    object Register : Screen("register")
    object CreateRecipe : Screen("create_recipe?recipeId={recipeId}") {
        /** Без id — новый рецепт, с id — правка существующего. */
        fun createRoute(recipeId: Int? = null) =
            if (recipeId == null) "create_recipe" else "create_recipe?recipeId=$recipeId"
    }
    object Journal : Screen("journal")
    object DeviceSetup : Screen("device_setup")
    object Cooking : Screen("cooking/{recipeId}?servings={servings}") {
        fun createRoute(recipeId: Int, servings: Int) = "cooking/$recipeId?servings=$servings"
    }
    object AiRecipe : Screen("ai_recipe")
    object ProductScan : Screen("product_scan")
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: Int) = "recipe_detail/$recipeId"
    }
}
