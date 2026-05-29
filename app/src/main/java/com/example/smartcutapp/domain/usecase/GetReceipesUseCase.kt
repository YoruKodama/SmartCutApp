package com.example.smartcutapp.domain.usecase

import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.repository.RecipeRepository

class GetRecipeByIdUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(id: Int): Recipe? {
        return repository.getRecipeById(id)
    }
}