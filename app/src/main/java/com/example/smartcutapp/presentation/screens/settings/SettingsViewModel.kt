package com.example.smartcutapp.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.data.mqtt.MqttManager
import com.example.smartcutapp.data.remote.api.TokenStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    val isConnected = MqttManager.isConnected
    val deviceStatus = MqttManager.deviceStatus
    val esp32Online = MqttManager.esp32Online

    private val _brokerUrl = MutableStateFlow(PreferencesManager.mqttBrokerUrl)
    val brokerUrl: StateFlow<String> = _brokerUrl

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoggedIn = MutableStateFlow(TokenStorage.token.isNotEmpty())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun setBrokerUrl(url: String) {
        _brokerUrl.value = url
        MqttManager.brokerUrl = url
    }

    fun toggleConnection() {
        if (MqttManager.isConnected.value) {
            MqttManager.disconnect()
        } else {
            viewModelScope.launch {
                _isConnecting.value = true
                _error.value = null
                val result = MqttManager.connect()
                if (result.isFailure) {
                    _error.value = "Не удалось подключиться к брокеру: ${result.exceptionOrNull()?.message}"
                } else {
                    MqttManager.publish(MqttManager.TOPIC_COMMAND, """{"action":"ping"}""")
                    delay(3000)
                    if (!MqttManager.esp32Online.value) {
                        _error.value = "Брокер доступен, но ESP32 не отвечает. Убедитесь, что устройство включено."
                    }
                }
                _isConnecting.value = false
            }
        }
    }

    fun logout() {
        TokenStorage.token = ""
        MqttManager.disconnect()
        _isLoggedIn.value = false
    }
}
