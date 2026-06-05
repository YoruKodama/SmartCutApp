package com.example.smartcutapp.data.repository

import com.example.smartcutapp.data.mapper.toRecipe
import com.example.smartcutapp.data.remote.api.ApiClient
import com.example.smartcutapp.data.remote.dto.RecipeResponseDto
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.repository.RecipeRepository
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RecipeRepositoryImpl(private val token: String) : RecipeRepository {

    override suspend fun getRecipes(): List<Recipe> {
        val response = ApiClient.client.get("${ApiClient.BASE_URL}/recipes") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body<List<RecipeResponseDto>>().map { it.toRecipe() }
    }

    override suspend fun getRecipeById(id: Int): Recipe? {
        val response = ApiClient.client.get("${ApiClient.BASE_URL}/recipes/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body<RecipeResponseDto>().toRecipe()
    }
}