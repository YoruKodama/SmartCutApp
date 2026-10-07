package com.example.smartcutapp.presentation.screens.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.local.RecipeCache
import com.example.smartcutapp.data.mapper.RecipeExchange
import com.example.smartcutapp.data.remote.api.TokenStorage
import com.example.smartcutapp.data.repository.RecipeRepositoryImpl
import com.example.smartcutapp.domain.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipesViewModel : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /** Показаны сохранённые на телефоне рецепты — сервер недоступен. */
    private val _offline = MutableStateFlow(false)
    val offline: StateFlow<Boolean> = _offline

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = RecipeRepositoryImpl(TokenStorage.token).getRecipes()
                _recipes.value = result
                _offline.value = RecipeCache.servedFromCache
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun exportJson(): String = RecipeExchange.encode(_recipes.value)

    fun exportedCount(): Int = _recipes.value.size

    /** Создаёт на сервере рецепты из файла. Возвращает сообщение о результате через [message]. */
    fun importJson(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val drafts = RecipeExchange.decode(text)
                val repo = RecipeRepositoryImpl(TokenStorage.token)
                var ok = 0
                var failed = 0
                drafts.forEach {
                    try {
                        repo.saveRecipe(null, it)
                        ok++
                    } catch (e: Exception) {
                        failed++
                    }
                }
                _message.value = "Импортировано рецептов: $ok" + if (failed > 0) ", с ошибкой: $failed" else ""
                loadRecipes()
            } catch (e: IllegalArgumentException) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showMessage(text: String) { _message.value = text }

    fun clearMessage() { _message.value = null }
}
