package com.example.smartcutapp.domain.usecase

import com.example.smartcutapp.domain.model.CutType
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Nutrition
import com.example.smartcutapp.domain.model.ProductNutrition
import com.example.smartcutapp.domain.model.Recipe
import org.junit.Assert.*
import org.junit.Test

class RecipePlannerTest {

    private fun ing(id: Int, name: String, amount: String = "", grams: Int? = null, cut: CutType? = null) =
        Ingredient(id, name, amount, cuttable = cut != null, grams = grams, cutType = cut)

    private val recipe = Recipe(
        id = 1, name = "Салат", cookingTime = "", imageUrl = null, servings = 2,
        ingredients = listOf(
            ing(1, "Огурец", grams = 200, cut = CutType.STICKS),
            ing(2, "Морковь", grams = 100, cut = CutType.GRATE),
            ing(3, "Помидор", grams = 300, cut = CutType.STICKS),
            ing(4, "Соль", amount = "по вкусу")
        )
    )

    @Test fun `scaling multiplies by servings ratio`() {
        assertEquals(400, RecipePlanner.scaleGrams(200, 2, 4))
        assertEquals(100, RecipePlanner.scaleGrams(200, 2, 1))
    }

    @Test fun `parseGrams understands grams and kilograms`() {
        assertEquals(300, RecipePlanner.parseGrams("300 г"))
        assertEquals(1500, RecipePlanner.parseGrams("1.5 кг"))
        assertNull(RecipePlanner.parseGrams("2 шт"))
        assertNull(RecipePlanner.parseGrams("по вкусу"))
    }

    @Test fun `same attachment ingredients are grouped in first appearance order`() {
        val plan = RecipePlanner.attachmentPlan(recipe)
        assertEquals(listOf(CutType.STICKS, CutType.GRATE), plan.map { it.cutType })
        assertEquals(listOf("Огурец", "Помидор"), plan[0].ingredients.map { it.name })
    }

    @Test fun `filter requires all attachments`() {
        assertTrue(RecipePlanner.matchesAttachments(recipe, setOf(CutType.STICKS, CutType.GRATE)))
        assertFalse(RecipePlanner.matchesAttachments(recipe, setOf(CutType.STICKS)))
    }

    @Test fun `total nutrition uses scaled grams`() {
        val products = mapOf(1 to ProductNutrition("Огурец", Nutrition(kcal = 15.0)))
        // 200 г на 2 порции -> 400 г на 4: 400/100*15 = 60
        assertEquals(60.0, RecipePlanner.totalNutrition(recipe, 4, products).kcal, 0.001)
    }

    @Test fun `ingredient without grams falls back to ai estimate`() {
        val products = mapOf(4 to ProductNutrition("Соль", Nutrition(kcal = 0.0), estimatedGrams = 3))
        assertEquals(0.0, RecipePlanner.totalNutrition(recipe, 2, products).kcal, 0.001)
    }
}
