package com.example.smartcutapp.domain.model

import org.junit.Assert.*
import org.junit.Test

class DomainModelsTest {

    @Test
    fun `recipe data class equality`() {
        val r1 = Recipe(id = 1, name = "Борщ", ingredients = emptyList(), cookingTime = "1 ч", imageUrl = null)
        val r2 = Recipe(id = 1, name = "Борщ", ingredients = emptyList(), cookingTime = "1 ч", imageUrl = null)
        assertEquals(r1, r2)
    }

    @Test
    fun `recipe with different ids are not equal`() {
        val r1 = Recipe(id = 1, name = "Борщ", ingredients = emptyList(), cookingTime = "", imageUrl = null)
        val r2 = Recipe(id = 2, name = "Борщ", ingredients = emptyList(), cookingTime = "", imageUrl = null)
        assertNotEquals(r1, r2)
    }

    @Test
    fun `recipe copy changes name`() {
        val original = Recipe(id = 1, name = "Борщ", ingredients = emptyList(), cookingTime = "", imageUrl = null)
        val copy = original.copy(name = "Щи")
        assertEquals("Щи", copy.name)
        assertEquals(original.id, copy.id)
        assertEquals(original.cookingTime, copy.cookingTime)
    }

    @Test
    fun `ingredient default cuttable is false`() {
        val ing = Ingredient(id = 1, name = "Соль", amount = "1 ч.л")
        assertFalse(ing.cuttable)
    }

    @Test
    fun `ingredient with cuttable true`() {
        val ing = Ingredient(id = 2, name = "Морковь", amount = "100 г", cuttable = true)
        assertTrue(ing.cuttable)
    }

    @Test
    fun `ingredient data class equality`() {
        val a = Ingredient(id = 1, name = "Лук", amount = "1 шт", cuttable = true)
        val b = Ingredient(id = 1, name = "Лук", amount = "1 шт", cuttable = true)
        assertEquals(a, b)
    }

    @Test
    fun `user with null token`() {
        val user = User(id = 1, name = "Алексей", token = null)
        assertNull(user.token)
    }

    @Test
    fun `user data class equality`() {
        val u1 = User(id = 5, name = "Иван", token = "abc123")
        val u2 = User(id = 5, name = "Иван", token = "abc123")
        assertEquals(u1, u2)
    }

    @Test
    fun `user copy changes token`() {
        val user = User(id = 1, name = "Петр", token = "old_token")
        val updated = user.copy(token = "new_token")
        assertEquals("new_token", updated.token)
        assertEquals(user.id, updated.id)
        assertEquals(user.name, updated.name)
    }
}
