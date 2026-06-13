package com.example.smartcutapp.presentation.screens.weighing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.mqtt.MqttManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

enum class WeightUnit { GRAMS, KG }

class WeighingViewModel : ViewModel() {

    val isConnected: StateFlow<Boolean> = MqttManager.isConnected
    val esp32Online: StateFlow<Boolean> = MqttManager.esp32Online

    private val _unit = MutableStateFlow(WeightUnit.GRAMS)
    val unit: StateFlow<WeightUnit> = _unit

    private val _tareOffset = MutableStateFlow(0.0f)

    val displayWeight: StateFlow<Float> = combine(
        MqttManager.weightGrams, _tareOffset
    ) { raw, tare -> (raw - tare).coerceAtLeast(0.0f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0f)

    fun setUnit(unit: WeightUnit) {
        _unit.value = unit
    }

    fun tare() {
        _tareOffset.value = MqttManager.weightGrams.value
    }

    fun resetTare() {
        _tareOffset.value = 0.0f
    }

    fun sendTareToDevice() {
        viewModelScope.launch {
            MqttManager.publish(MqttManager.TOPIC_COMMAND, """{"action":"tare"}""")
        }
    }
}
