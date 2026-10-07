package com.example.smartcutapp.data.mapper

import com.example.smartcutapp.domain.model.CutType
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Recipe
import org.junit.Assert.*
import org.junit.Test

class RecipeExchangeTest {

    @Test fun `export then import keeps recipe data`() {
        val recipe = Recipe(
            1, "Салат", listOf(Ingredient(5, "Огурец", "2 шт", true, 250, CutType.STICKS)),
            "10 мин", null, servings = 3, steps = listOf("Нарезать", "Смешать"), tags = listOf("салат", "быстро")
        )
        val d = RecipeExchange.decode(RecipeExchange.encode(listOf(recipe))).single()
        assertEquals("Салат", d.name)
        assertEquals(3, d.servings)
        assertEquals(listOf("Нарезать", "Смешать"), d.steps)
        assertEquals(listOf("салат", "быстро"), d.tags)
        assertEquals(CutType.STICKS, d.ingredients.single().cutType)
        assertEquals(250, d.ingredients.single().grams)
    }

    @Test fun `invalid files give readable errors`() {
        assertThrows(IllegalArgumentException::class.java) { RecipeExchange.decode("абракадабра") }
        assertThrows(IllegalArgumentException::class.java) { RecipeExchange.decode("[]") }
    }
}
