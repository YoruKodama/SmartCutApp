package com.example.smartcutapp.presentation.screens.recipe_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.ai.NutritionQuery
import com.example.smartcutapp.data.ai.NutritionRepository
import com.example.smartcutapp.data.remote.api.TokenStorage
import com.example.smartcutapp.data.repository.RecipeRepositoryImpl
import com.example.smartcutapp.domain.model.Nutrition
import com.example.smartcutapp.domain.model.ProductNutrition
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.usecase.RecipePlanner
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

    /** Число порций, на которое пересчитаны массы. */
    private val _servings = MutableStateFlow(1)
    val servings: StateFlow<Int> = _servings

    /** Оценка ккал/БЖУ на 100 г по id ингредиента (из кэша или от нейросети). */
    private val _products = MutableStateFlow<Map<Int, ProductNutrition>>(emptyMap())
    val products: StateFlow<Map<Int, ProductNutrition>> = _products

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating

    private val _nutritionError = MutableStateFlow<String?>(null)
    val nutritionError: StateFlow<String?> = _nutritionError

    private var servingsInitialized = false

    fun loadRecipe(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = RecipeRepositoryImpl(TokenStorage.token).getRecipeById(id)
                _recipe.value = result
                if (result != null) {
                    if (!servingsInitialized) {
                        _servings.value = result.servings
                        servingsInitialized = true
                    }
                    _products.value = cachedProducts(result)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setServings(value: Int) {
        _servings.value = value.coerceIn(1, 50)
    }

    /** Ккал и БЖУ всего блюда на выбранное число порций по тому, что уже оценено. */
    fun totalNutrition(recipe: Recipe, servings: Int, products: Map<Int, ProductNutrition>): Nutrition =
        RecipePlanner.totalNutrition(recipe, servings, products)

    /** Просит нейросеть оценить ингредиенты, которых ещё нет в кэше. */
    fun calculateNutrition() {
        val r = _recipe.value ?: return
        viewModelScope.launch {
            _isCalculating.value = true
            _nutritionError.value = null
            try {
                val queries = r.ingredients.map { NutritionQuery(it.name, it.amount) }
                val byIndex = NutritionRepository.estimate(queries)
                _products.value = byIndex.mapKeys { r.ingredients[it.key].id }
            } catch (e: Exception) {
                _nutritionError.value = e.message
            } finally {
                _isCalculating.value = false
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

    private fun cachedProducts(r: Recipe): Map<Int, ProductNutrition> {
        val byIndex = NutritionRepository.cached(r.ingredients.map { NutritionQuery(it.name, it.amount) })
        return byIndex.mapKeys { r.ingredients[it.key].id }
    }
}
