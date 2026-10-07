package com.example.smartcutapp.presentation.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.ai.DietPlanner
import com.example.smartcutapp.data.local.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DietPlanState(
    val isLoading: Boolean = false,
    val text: String? = null,
    val error: String? = null
)

/** Экран «Питание»: рацион от нейросети (сканирование продукта живёт в ProductScanViewModel). */
class JournalViewModel : ViewModel() {

    /** Последняя использованная цель по калориям — подставляется в поле рациона. */
    val defaultKcal: Int get() = PreferencesManager.dailyKcalGoal

    private val _plan = MutableStateFlow(DietPlanState())
    val plan: StateFlow<DietPlanState> = _plan

    fun buildPlan(targetKcal: Int, meals: Int, preferences: String) {
        val kcal = targetKcal.coerceIn(500, 10000)
        PreferencesManager.dailyKcalGoal = kcal
        viewModelScope.launch {
            _plan.value = DietPlanState(isLoading = true)
            _plan.value = try {
                DietPlanState(text = DietPlanner.plan(kcal, meals, preferences, alreadyEatenKcal = 0))
            } catch (e: Exception) {
                DietPlanState(error = e.message)
            }
        }
    }

    fun clearPlan() = _plan.update { DietPlanState() }
}
