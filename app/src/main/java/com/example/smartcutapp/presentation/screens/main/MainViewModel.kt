package com.example.smartcutapp.presentation.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.remote.api.TokenStorage
import com.example.smartcutapp.data.repository.RecipeRepositoryImpl
import com.example.smartcutapp.domain.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = RecipeRepositoryImpl(TokenStorage.token).getRecipes()
                _recipes.value = result
            } catch (e: Exception) {
                _recipes.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}