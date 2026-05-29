package com.example.smartcutapp.domain.repository

import com.example.smartcutapp.domain.model.Recipe

interface RecipeRepository {
    suspend fun getRecipes(): List<Recipe>
    suspend fun getRecipeById(id: Int): Recipe?
}