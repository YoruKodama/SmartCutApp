package com.example.smartcutapp.presentation.screens.create_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.remote.api.TokenStorage
import com.example.smartcutapp.data.repository.RecipeRepositoryImpl
import com.example.smartcutapp.domain.model.CutType
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.model.RecipeDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class IngredientDraft(
    val name: String = "",
    val amount: String = "",
    val cuttable: Boolean = false,
    /** Целевая масса на базовое число порций, г (пусто — не задана). */
    val grams: String = "",
    val cutType: CutType? = null
)

class CreateRecipeViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _created = MutableStateFlow(false)
    val created: StateFlow<Boolean> = _created

    /** Рецепт, загруженный для правки. */
    private val _editing = MutableStateFlow<Recipe?>(null)
    val editing: StateFlow<Recipe?> = _editing

    fun loadForEdit(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _editing.value = RecipeRepositoryImpl(TokenStorage.token).getRecipeById(id)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** [editId] != null — правка существующего рецепта (PUT), иначе создание. */
    fun createRecipe(
        name: String,
        cookingTime: String,
        imageBytes: ByteArray?,
        ingredients: List<IngredientDraft>,
        servings: Int = 1,
        steps: String = "",
        tags: String = "",
        editId: Int? = null
    ) {
        if (name.isBlank()) {
            _error.value = "Название рецепта не может быть пустым"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val repo = RecipeRepositoryImpl(TokenStorage.token)
                val imageUrl = if (imageBytes != null && imageBytes.isNotEmpty()) {
                    repo.uploadImage(imageBytes, "${System.currentTimeMillis()}.jpg")
                } else _editing.value?.imageUrl

                repo.saveRecipe(
                    id = editId,
                    draft = RecipeDraft(
                        name = name.trim(),
                        cookingTime = cookingTime.trim(),
                        imageUrl = imageUrl,
                        servings = servings.coerceAtLeast(1),
                        steps = steps.lines().map { it.trim() }.filter { it.isNotEmpty() },
                        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        ingredients = ingredients.filter { it.name.isNotBlank() }.mapIndexed { i, d ->
                            Ingredient(
                                id = i,
                                name = d.name.trim(),
                                amount = d.amount.trim(),
                                cuttable = d.cutType != null || d.cuttable,
                                grams = d.grams.trim().toIntOrNull()?.takeIf { it > 0 },
                                cutType = d.cutType
                            )
                        }
                    )
                )
                _created.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
