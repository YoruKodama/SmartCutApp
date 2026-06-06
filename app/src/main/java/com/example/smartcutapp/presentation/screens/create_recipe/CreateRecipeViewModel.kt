package com.example.smartcutapp.presentation.screens.create_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.remote.api.TokenStorage
import com.example.smartcutapp.data.repository.RecipeRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class IngredientDraft(
    val name: String = "",
    val amount: String = "",
    val cuttable: Boolean = false
)

class CreateRecipeViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _created = MutableStateFlow(false)
    val created: StateFlow<Boolean> = _created

    fun createRecipe(
        name: String,
        cookingTime: String,
        imageBytes: ByteArray?,
        ingredients: List<IngredientDraft>
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
                } else null

                repo.createRecipe(
                    name = name.trim(),
                    cookingTime = cookingTime.trim().ifEmpty { null },
                    imageUrl = imageUrl,
                    ingredients = ingredients
                        .filter { it.name.isNotBlank() }
                        .map { Triple(it.name.trim(), it.amount.trim().ifEmpty { null }, it.cuttable) }
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
