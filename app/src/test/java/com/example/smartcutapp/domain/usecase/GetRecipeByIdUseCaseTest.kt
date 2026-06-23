package com.example.smartcutapp.domain.usecase

import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.repository.RecipeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetRecipeByIdUseCaseTest {

    private val recipe = Recipe(
        id = 7, name = "Стейк", ingredients = emptyList(),
        cookingTime = "20 мин", imageUrl = null
    )

    private fun repoWith(found: Recipe?) = object : RecipeRepository {
        override suspend fun getRecipes() = emptyList<Recipe>()
        override suspend fun getRecipeById(id: Int) = found
        override suspend fun createRecipe(
            name: String, cookingTime: String?, imageUrl: String?,
            ingredients: List<Triple<String, String?, Boolean>>
        ) = error("not used")
        override suspend fun deleteRecipe(id: Int) {}
        override suspend fun uploadImage(bytes: ByteArray, fileName: String) = ""
    }

    @Test
    fun `invoke returns recipe when found`() = runTest {
        val result = GetRecipeByIdUseCase(repoWith(recipe))(7)
        assertEquals(recipe, result)
    }

    @Test
    fun `invoke returns null when recipe not found`() = runTest {
        val result = GetRecipeByIdUseCase(repoWith(null))(99)
        assertNull(result)
    }

    @Test
    fun `invoke passes correct id to repository`() = runTest {
        var capturedId = -1
        val capturingRepo = object : RecipeRepository {
            override suspend fun getRecipes() = emptyList<Recipe>()
            override suspend fun getRecipeById(id: Int): Recipe? {
                capturedId = id
                return null
            }
            override suspend fun createRecipe(
                name: String, cookingTime: String?, imageUrl: String?,
                ingredients: List<Triple<String, String?, Boolean>>
            ) = error("not used")
            override suspend fun deleteRecipe(id: Int) {}
            override suspend fun uploadImage(bytes: ByteArray, fileName: String) = ""
        }
        GetRecipeByIdUseCase(capturingRepo)(42)
        assertEquals(42, capturedId)
    }

    @Test
    fun `invoke propagates exception from repository`() = runTest {
        val errorRepo = object : RecipeRepository {
            override suspend fun getRecipes() = emptyList<Recipe>()
            override suspend fun getRecipeById(id: Int) = throw RuntimeException("Нет доступа")
            override suspend fun createRecipe(
                name: String, cookingTime: String?, imageUrl: String?,
                ingredients: List<Triple<String, String?, Boolean>>
            ) = error("not used")
            override suspend fun deleteRecipe(id: Int) {}
            override suspend fun uploadImage(bytes: ByteArray, fileName: String) = ""
        }
        val ex = runCatching { GetRecipeByIdUseCase(errorRepo)(1) }.exceptionOrNull()
        assertNotNull(ex)
        assertEquals("Нет доступа", ex?.message)
    }
}
