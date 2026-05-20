package com.example.smartcutapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartcutapp.presentation.screens.main.MainScreen
import com.example.smartcutapp.presentation.screens.recipes.RecipesScreen


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("recipes") { RecipesScreen(navController) }
    }
}