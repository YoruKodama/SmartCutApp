package com.example.smartcutapp.presentation.screens.blade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.mqtt.MqttManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SliceSettingsViewModel : ViewModel() {

    val isConnected = MqttManager.isConnected

    private val _thickness = MutableStateFlow(5)
    val thickness: StateFlow<Int> = _thickness

    private val _speed = MutableStateFlow(0.3f)
    val speed: StateFlow<Float> = _speed

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _sendResult = MutableStateFlow<String?>(null)
    val sendResult: StateFlow<String?> = _sendResult

    fun setThickness(v: Int) { _thickness.value = v }
    fun setSpeed(v: Float) { _speed.value = v }

    fun sendCommand() {
        viewModelScope.launch {
            _isSending.value = true
            val payload = """{"mode":"slice","thickness":${_thickness.value},"speed":${"%.2f".format(_speed.value)}}"""
            val result = MqttManager.publish(MqttManager.TOPIC_COMMAND, payload)
            _sendResult.value = if (result.isSuccess) "Команда отправлена"
            else "Ошибка: ${result.exceptionOrNull()?.message}"
            _isSending.value = false
        }
    }

    fun clearResult() { _sendResult.value = null }
}
