package com.example.smartcutapp.data.mapper

import com.example.smartcutapp.data.remote.dto.IngredientResponseDto
import com.example.smartcutapp.data.remote.dto.RecipeResponseDto
import org.junit.Assert.*
import org.junit.Test

class RecipeMapperTest {

    @Test
    fun `toRecipe maps all fields correctly`() {
        val dto = RecipeResponseDto(
            id = 1,
            name = "Борщ",
            cookingTime = "60 мин",
            imageUrl = "http://example.com/img.jpg",
            userId = 42,
            ingredients = emptyList()
        )
        val recipe = dto.toRecipe()
        assertEquals(1, recipe.id)
        assertEquals("Борщ", recipe.name)
        assertEquals("60 мин", recipe.cookingTime)
        assertEquals("http://example.com/img.jpg", recipe.imageUrl)
        assertTrue(recipe.ingredients.isEmpty())
    }

    @Test
    fun `toRecipe uses empty string when cookingTime is null`() {
        val dto = RecipeResponseDto(id = 2, name = "Пицца", cookingTime = null, imageUrl = null, userId = 1)
        val recipe = dto.toRecipe()
        assertEquals("", recipe.cookingTime)
    }

    @Test
    fun `toRecipe preserves null imageUrl`() {
        val dto = RecipeResponseDto(id = 3, name = "Суп", userId = 1)
        val recipe = dto.toRecipe()
        assertNull(recipe.imageUrl)
    }

    @Test
    fun `toIngredient maps all fields correctly`() {
        val dto = IngredientResponseDto(id = 10, name = "Морковь", amount = "2 шт", cuttable = true)
        val ingredient = dto.toIngredient()
        assertEquals(10, ingredient.id)
        assertEquals("Морковь", ingredient.name)
        assertEquals("2 шт", ingredient.amount)
        assertTrue(ingredient.cuttable)
    }

    @Test
    fun `toIngredient uses empty string when amount is null`() {
        val dto = IngredientResponseDto(id = 11, name = "Соль", amount = null, cuttable = false)
        val ingredient = dto.toIngredient()
        assertEquals("", ingredient.amount)
        assertFalse(ingredient.cuttable)
    }

    @Test
    fun `toRecipe maps nested ingredients`() {
        val dto = RecipeResponseDto(
            id = 5,
            name = "Салат",
            userId = 1,
            ingredients = listOf(
                IngredientResponseDto(id = 1, name = "Огурец", amount = "1 шт", cuttable = true),
                IngredientResponseDto(id = 2, name = "Помидор", amount = "2 шт", cuttable = false)
            )
        )
        val recipe = dto.toRecipe()
        assertEquals(2, recipe.ingredients.size)
        assertEquals("Огурец", recipe.ingredients[0].name)
        assertTrue(recipe.ingredients[0].cuttable)
        assertEquals("Помидор", recipe.ingredients[1].name)
        assertFalse(recipe.ingredients[1].cuttable)
    }
}
