package com.example.smartcutapp.data.repository

import com.example.smartcutapp.data.local.RecipeCache
import com.example.smartcutapp.data.mapper.toRecipe
import com.example.smartcutapp.data.mapper.toRequest
import com.example.smartcutapp.data.remote.api.ApiClient
import com.example.smartcutapp.data.remote.dto.RecipeResponseDto
import com.example.smartcutapp.data.remote.dto.UploadResponseDto
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.model.RecipeDraft
import com.example.smartcutapp.domain.repository.RecipeRepository
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class RecipeRepositoryImpl(private val token: String) : RecipeRepository {

    /** С сервера; если сервер недоступен — из локального кэша (если он есть). */
    override suspend fun getRecipes(): List<Recipe> {
        return try {
            val dtos = ApiClient.client.get("${ApiClient.BASE_URL}/recipes") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body<List<RecipeResponseDto>>()
            RecipeCache.save(dtos)
            RecipeCache.servedFromCache = false
            dtos.map { it.toRecipe() }
        } catch (e: Exception) {
            val cached = RecipeCache.load()
            if (cached.isEmpty()) throw e
            RecipeCache.servedFromCache = true
            cached.map { it.toRecipe() }
        }
    }

    override suspend fun getRecipeById(id: Int): Recipe? {
        return try {
            ApiClient.client.get("${ApiClient.BASE_URL}/recipes/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body<RecipeResponseDto>().toRecipe()
        } catch (e: Exception) {
            RecipeCache.load().firstOrNull { it.id == id }?.toRecipe() ?: throw e
        }
    }

    override suspend fun createRecipe(
        name: String,
        cookingTime: String?,
        imageUrl: String?,
        ingredients: List<Triple<String, String?, Boolean>>
    ): Recipe = saveRecipe(
        id = null,
        draft = RecipeDraft(
            name = name,
            cookingTime = cookingTime.orEmpty(),
            imageUrl = imageUrl,
            ingredients = ingredients.mapIndexed { i, (n, a, c) ->
                Ingredient(id = i, name = n, amount = a.orEmpty(), cuttable = c)
            }
        )
    )

    /** Создаёт рецепт (id == null) или правит существующий (PUT). */
    suspend fun saveRecipe(id: Int?, draft: RecipeDraft): Recipe {
        val url = if (id == null) "${ApiClient.BASE_URL}/recipes" else "${ApiClient.BASE_URL}/recipes/$id"
        val response = ApiClient.client.request(url) {
            method = if (id == null) HttpMethod.Post else HttpMethod.Put
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(draft.toRequest())
        }
        if (!response.status.isSuccess()) {
            error("Сервер отклонил рецепт (${response.status.value}): ${response.bodyAsText()}")
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
