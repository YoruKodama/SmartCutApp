package com.example.smartcutapp.presentation.screens.create_recipe

import org.junit.Assert.*
import org.junit.Test

class IngredientDraftTest {

    @Test
    fun `default values are empty strings and false`() {
        val draft = IngredientDraft()
        assertEquals("", draft.name)
        assertEquals("", draft.amount)
        assertFalse(draft.cuttable)
    }

    @Test
    fun `data class equality with same values`() {
        val a = IngredientDraft("Морковь", "100 г", true)
        val b = IngredientDraft("Морковь", "100 г", true)
        assertEquals(a, b)
    }

    @Test
    fun `data class not equal when cuttable differs`() {
        val a = IngredientDraft("Морковь", "100 г", true)
        val b = IngredientDraft("Морковь", "100 г", false)
        assertNotEquals(a, b)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val draft = IngredientDraft("Соль", "1 ч.л", false)
        val updated = draft.copy(cuttable = true)
        assertTrue(updated.cuttable)
        assertEquals("Соль", updated.name)
        assertEquals("1 ч.л", updated.amount)
    }

    @Test
    fun `copy changes name only`() {
        val draft = IngredientDraft("Лук", "50 г", false)
        val updated = draft.copy(name = "Чеснок")
        assertEquals("Чеснок", updated.name)
        assertEquals("50 г", updated.amount)
        assertFalse(updated.cuttable)
    }
}
