package com.example.smartcutapp.domain.repository

import com.example.smartcutapp.domain.model.Recipe

interface RecipeRepository {
    suspend fun getRecipes(): List<Recipe>
    suspend fun getRecipeById(id: Int): Recipe?
    suspend fun createRecipe(name: String, cookingTime: String?, imageUrl: String?, ingredients: List<Triple<String, String?, Boolean>>): Recipe
    suspend fun deleteRecipe(id: Int)
    suspend fun uploadImage(bytes: ByteArray, fileName: String): String
}