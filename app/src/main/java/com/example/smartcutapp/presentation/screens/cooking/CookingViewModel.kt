package com.example.smartcutapp.presentation.screens.cooking

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.ai.NutritionQuery
import com.example.smartcutapp.data.ai.NutritionRepository
import com.example.smartcutapp.data.ai.ProductRecognizer
import com.example.smartcutapp.data.local.JournalStorage
import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.data.mqtt.DeviceCommands
import com.example.smartcutapp.data.mqtt.MqttManager
import com.example.smartcutapp.data.remote.api.TokenStorage
import com.example.smartcutapp.data.repository.RecipeRepositoryImpl
import com.example.smartcutapp.domain.model.CutType
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Nutrition
import com.example.smartcutapp.domain.model.ProductNutrition
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.usecase.RecipePlanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StepStatus { PENDING, SENT, DONE, SKIPPED }

data class CookStep(
    val ingredient: Ingredient,
    val cutType: CutType,
    /** Масса на выбранное число порций, г. */
    val grams: Int?,
    /** Насадку нужно сменить по сравнению с предыдущим шагом (для первого шага — поставить). */
    val changeAttachment: Boolean
)

data class CookingState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val recipe: Recipe? = null,
    val servings: Int = 1,
    val steps: List<CookStep> = emptyList(),
    val statuses: List<StepStatus> = emptyList(),
    val current: Int = 0,
    /** Ингредиенты без нарезки — добавляются вручную. */
    val manualIngredients: List<Ingredient> = emptyList(),
    val products: Map<Int, ProductNutrition> = emptyMap(),
    val commandMessage: String? = null,
    val checkMessage: String? = null,
    val isChecking: Boolean = false,
    val nutritionLoading: Boolean = false,
    val nutritionError: String? = null,
    val journaled: Boolean = false
) {
    val finished: Boolean
        get() = steps.isNotEmpty() && statuses.all { it == StepStatus.DONE || it == StepStatus.SKIPPED }

    /** Ккал/БЖУ блюда: все ингредиенты, кроме пропущенных шагов. */
    val total: Nutrition
        get() {
            val r = recipe ?: return Nutrition.ZERO
            val skipped = steps.indices.filter { statuses.getOrNull(it) == StepStatus.SKIPPED }
                .map { steps[it].ingredient.id }.toSet()
            val eaten = r.copy(ingredients = r.ingredients.filter { it.id !in skipped })
            return RecipePlanner.totalNutrition(eaten, servings, products)
        }
}

class CookingViewModel : ViewModel() {

    private val _state = MutableStateFlow(CookingState())
    val state: StateFlow<CookingState> = _state

    val isConnected: StateFlow<Boolean> = MqttManager.isConnected
    val esp32Online: StateFlow<Boolean> = MqttManager.esp32Online
    val linkLost: StateFlow<Boolean> = MqttManager.linkLost

    private val _autoConfirm = MutableStateFlow(PreferencesManager.autoConfirm)
    val autoConfirm: StateFlow<Boolean> = _autoConfirm

    private var started = false

    fun start(recipeId: Int, servingsArg: Int) {
        if (started) return
        started = true
        viewModelScope.launch {
            // Связь с устройством поднимаем сами, если ещё не подключены
            if (!MqttManager.isConnected.value) launch { MqttManager.connect() }
            try {
                val recipe = RecipeRepositoryImpl(TokenStorage.token).getRecipeById(recipeId)
                    ?: error("Рецепт не найден")
                val servings = if (servingsArg > 0) servingsArg else recipe.servings
                val steps = buildSteps(recipe, servings)
                val queries = recipe.ingredients.map { NutritionQuery(it.name, it.amount) }
                val cached = NutritionRepository.cached(queries).mapKeys { recipe.ingredients[it.key].id }
                _state.value = CookingState(
                    isLoading = false,
                    recipe = recipe,
                    servings = servings,
                    steps = steps,
                    statuses = steps.map { StepStatus.PENDING },
                    manualIngredients = recipe.ingredients.filter { it.cutType == null },
                    products = cached
                )
                if (steps.isEmpty()) _state.update { it.copy(error = "В рецепте нет ингредиентов для нарезки") }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Не удалось загрузить рецепт") }
            }
        }
    }

    private fun buildSteps(recipe: Recipe, servings: Int): List<CookStep> {
        var previous: CutType? = null
        return RecipePlanner.attachmentPlan(recipe).flatMap { group ->
            group.ingredients.map { ing ->
                CookStep(
                    ingredient = ing,
                    cutType = group.cutType,
                    grams = RecipePlanner.scaledGrams(recipe, ing, servings),
                    changeAttachment = group.cutType != previous
                ).also { previous = group.cutType }
            }
        }
    }

    fun goTo(index: Int) = _state.update {
        if (index in it.steps.indices) it.copy(current = index, checkMessage = null, commandMessage = null) else it
    }

    /** Отправляет устройству текущий ингредиент. Старт нарезки подтверждается на устройстве. */
    fun sendCurrentStep() {
        val s = _state.value
        val step = s.steps.getOrNull(s.current) ?: return
        viewModelScope.launch {
            val payload = DeviceCommands.recipeStep(
                step.ingredient.name, step.cutType, step.grams, _autoConfirm.value
            )
            val result = MqttManager.sendCommand(payload)
            _state.update {
                it.copy(
                    commandMessage = if (result.isSuccess) {
                        if (_autoConfirm.value) "Отправлено. Старт подтвердится автоматически"
                        else "Отправлено. Подтвердите старт на экране устройства"
                    } else result.exceptionOrNull()?.message ?: "Не удалось отправить",
                    statuses = if (result.isSuccess) it.statuses.setAt(it.current, StepStatus.SENT) else it.statuses
                )
            }
        }
    }

    fun cancelCurrentStep() {
        viewModelScope.launch {
            val result = MqttManager.sendCommand(DeviceCommands.clearStep())
            _state.update {
                it.copy(
                    commandMessage = if (result.isSuccess) "Шаг отменён на устройстве" else result.exceptionOrNull()?.message,
                    statuses = it.statuses.setAt(it.current, StepStatus.PENDING)
                )
            }
        }
    }

    fun markDone() = finishStep(StepStatus.DONE)

    fun skip() = finishStep(StepStatus.SKIPPED)

    private fun finishStep(status: StepStatus) {
        _state.update { s ->
            val statuses = s.statuses.setAt(s.current, status)
            val next = statuses.indices.firstOrNull { i -> i > s.current && statuses[i] != StepStatus.DONE && statuses[i] != StepStatus.SKIPPED }
                ?: statuses.indices.firstOrNull { statuses[it] != StepStatus.DONE && statuses[it] != StepStatus.SKIPPED }
                ?: s.current
            s.copy(statuses = statuses, current = next, checkMessage = null, commandMessage = null)
        }
        if (_state.value.finished && _state.value.products.isEmpty()) calculateNutrition()
    }

    /** Фото с камеры телефона + нейросеть Ollama: тот ли продукт у вас в руках. */
    fun checkProduct(context: Context, photo: Uri) {
        val s = _state.value
        val name = s.steps.getOrNull(s.current)?.ingredient?.name ?: return
        val appContext = context.applicationContext
        viewModelScope.launch {
            _state.update { it.copy(isChecking = true, checkMessage = null) }
            val message = try {
                val image = ProductRecognizer.loadImage(appContext, photo)
                when (ProductRecognizer.isProduct(image, name)) {
                    true -> "✔ Распознано: $name"
                    false -> "⚠ Похоже, это не «$name». Проверьте продукт или исправьте выбор"
                    null -> "Не удалось уверенно распознать продукт"
                }
            } catch (e: Exception) {
                e.message ?: "Ошибка распознавания"
            }
            _state.update { it.copy(isChecking = false, checkMessage = message) }
        }
    }

    fun setAutoConfirm(value: Boolean) {
        _autoConfirm.value = value
        PreferencesManager.autoConfirm = value
    }

    fun calculateNutrition() {
        val r = _state.value.recipe ?: return
        viewModelScope.launch {
            _state.update { it.copy(nutritionLoading = true, nutritionError = null) }
            try {
                val byIndex = NutritionRepository.estimate(r.ingredients.map { NutritionQuery(it.name, it.amount) })
                _state.update { it.copy(products = byIndex.mapKeys { e -> r.ingredients[e.key].id }) }
            } catch (e: Exception) {
                _state.update { it.copy(nutritionError = e.message) }
            } finally {
                _state.update { it.copy(nutritionLoading = false) }
            }
        }
    }

    fun saveToJournal() {
        val s = _state.value
        val r = s.recipe ?: return
        if (s.journaled) return
        JournalStorage.add(
            title = "${r.name} (${s.servings} порц.)",
            nutrition = s.total,
            source = "recipe"
        )
        _state.update { it.copy(journaled = true) }
    }

    private fun List<StepStatus>.setAt(i: Int, v: StepStatus) =
        mapIndexed { idx, old -> if (idx == i) v else old }
}
