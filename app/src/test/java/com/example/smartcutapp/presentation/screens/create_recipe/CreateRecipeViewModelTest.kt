package com.example.smartcutapp.presentation.screens.create_recipe

import org.junit.Assert.*
import org.junit.Test

class CreateRecipeViewModelTest {

    private fun vm() = CreateRecipeViewModel()

    @Test
    fun `blank name sets error without starting network call`() {
        val vm = vm()
        vm.createRecipe("", "30 мин", null, emptyList())
        assertEquals("Название рецепта не может быть пустым", vm.error.value)
    }

    @Test
    fun `whitespace only name is treated as blank`() {
        val vm = vm()
        vm.createRecipe("   ", "30 мин", null, emptyList())
        assertEquals("Название рецепта не может быть пустым", vm.error.value)
    }

    @Test
    fun `blank name does not trigger loading state`() {
        val vm = vm()
        vm.createRecipe("", "30 мин", null, emptyList())
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `blank name does not set created to true`() {
        val vm = vm()
        vm.createRecipe("", "30 мин", null, emptyList())
        assertFalse(vm.created.value)
    }

    @Test
    fun `initial state has no error`() {
        val vm = vm()
        assertNull(vm.error.value)
    }

    @Test
    fun `initial state is not loading`() {
        val vm = vm()
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `initial state is not created`() {
        val vm = vm()
        assertFalse(vm.created.value)
    }
}
