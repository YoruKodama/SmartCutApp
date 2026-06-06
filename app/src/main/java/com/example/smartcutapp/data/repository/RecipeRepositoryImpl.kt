package com.example.smartcutapp.data.repository

import com.example.smartcutapp.data.mapper.toRecipe
import com.example.smartcutapp.data.remote.api.ApiClient
import com.example.smartcutapp.data.remote.dto.IngredientRequestDto
import com.example.smartcutapp.data.remote.dto.RecipeRequestDto
import com.example.smartcutapp.data.remote.dto.RecipeResponseDto
import com.example.smartcutapp.data.remote.dto.UploadResponseDto
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.repository.RecipeRepository
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
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

    override suspend fun createRecipe(
        name: String,
        cookingTime: String?,
        imageUrl: String?,
        ingredients: List<Triple<String, String?, Boolean>>
    ): Recipe {
        val body = RecipeRequestDto(
            name = name,
            cookingTime = cookingTime,
            imageUrl = imageUrl,
            ingredients = ingredients.map { (n, a, c) -> IngredientRequestDto(n, a, c) }
        )
        val response = ApiClient.client.post("${ApiClient.BASE_URL}/recipes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.body<RecipeResponseDto>().toRecipe()
    }

    override suspend fun deleteRecipe(id: Int) {
        ApiClient.client.delete("${ApiClient.BASE_URL}/recipes/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String): String {
        val response = ApiClient.client.post("${ApiClient.BASE_URL}/images/upload") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(MultiPartFormDataContent(
                formData {
                    append("image", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ))
        }
        return response.body<UploadResponseDto>().url
    }
}
