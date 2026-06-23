package com.example.smartcutapp.domain.usecase

import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.repository.RecipeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetRecipesUseCaseTest {

    private fun fakeRecipe(id: Int, name: String) = Recipe(
        id = id, name = name, ingredients = emptyList(), cookingTime = "", imageUrl = null
    )

    private fun repoReturning(recipes: List<Recipe>) = object : RecipeRepository {
        override suspend fun getRecipes() = recipes
        override suspend fun getRecipeById(id: Int) = recipes.find { it.id == id }
        override suspend fun createRecipe(
            name: String, cookingTime: String?, imageUrl: String?,
            ingredients: List<Triple<String, String?, Boolean>>
        ) = recipes.first()
        override suspend fun deleteRecipe(id: Int) {}
        override suspend fun uploadImage(bytes: ByteArray, fileName: String) = "http://url"
    }

    @Test
    fun `invoke returns list of recipes`() = runTest {
        val expected = listOf(fakeRecipe(1, "Борщ"), fakeRecipe(2, "Пицца"))
        val result = GetRecipesUseCase(repoReturning(expected))()
        assertEquals(expected, result)
    }

    @Test
    fun `invoke returns empty list when no recipes exist`() = runTest {
        val result = GetRecipesUseCase(repoReturning(emptyList()))()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke returns single recipe`() = runTest {
        val single = listOf(fakeRecipe(42, "Стейк"))
        val result = GetRecipesUseCase(repoReturning(single))()
        assertEquals(1, result.size)
        assertEquals("Стейк", result.first().name)
    }

    @Test
    fun `invoke propagates exception from repository`() = runTest {
        val errorRepo = object : RecipeRepository {
            override suspend fun getRecipes() = throw RuntimeException("Ошибка сети")
            override suspend fun getRecipeById(id: Int) = null
            override suspend fun createRecipe(
                name: String, cookingTime: String?, imageUrl: String?,
                ingredients: List<Triple<String, String?, Boolean>>
            ) = error("")
            override suspend fun deleteRecipe(id: Int) {}
            override suspend fun uploadImage(bytes: ByteArray, fileName: String) = ""
        }
        val ex = runCatching { GetRecipesUseCase(errorRepo)() }.exceptionOrNull()
        assertNotNull(ex)
        assertEquals("Ошибка сети", ex?.message)
    }
}
