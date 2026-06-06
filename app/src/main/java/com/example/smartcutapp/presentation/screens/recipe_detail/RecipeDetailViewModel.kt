package com.example.smartcutapp.presentation.screens.recipe_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.remote.api.TokenStorage
import com.example.smartcutapp.data.repository.RecipeRepositoryImpl
import com.example.smartcutapp.domain.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel : ViewModel() {

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted

    fun loadRecipe(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = RecipeRepositoryImpl(TokenStorage.token).getRecipeById(id)
                _recipe.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRecipe(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                RecipeRepositoryImpl(TokenStorage.token).deleteRecipe(id)
                _deleted.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
