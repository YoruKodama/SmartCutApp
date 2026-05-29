package com.example.smartcutapp.domain.usecase

import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.repository.RecipeRepository

class GetRecipesUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(): List<Recipe> {
        return repository.getRecipes()
    }
}