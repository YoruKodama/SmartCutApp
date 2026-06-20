package com.example.smartcutapp.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.data.local.ThemeManager
import com.example.smartcutapp.data.local.ThemeMode
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

    val themeMode: StateFlow<ThemeMode> = ThemeManager.themeMode

    private val _mistralApiKey = MutableStateFlow(PreferencesManager.mistralApiKey)
    val mistralApiKey: StateFlow<String> = _mistralApiKey

    private val _esp32CamUrl = MutableStateFlow(PreferencesManager.esp32CamUrl)
    val esp32CamUrl: StateFlow<String> = _esp32CamUrl

    private val _ollamaUrl = MutableStateFlow(PreferencesManager.ollamaUrl)
    val ollamaUrl: StateFlow<String> = _ollamaUrl

    private val _ollamaModel = MutableStateFlow(PreferencesManager.ollamaModel)
    val ollamaModel: StateFlow<String> = _ollamaModel

    fun setBrokerUrl(url: String) {
        _brokerUrl.value = url
        MqttManager.brokerUrl = url
    }

    fun setThemeMode(mode: ThemeMode) {
        ThemeManager.setThemeMode(mode)
    }

    fun setMistralApiKey(key: String) {
        _mistralApiKey.value = key
        PreferencesManager.mistralApiKey = key
    }

    fun setEsp32CamUrl(url: String) {
        _esp32CamUrl.value = url
        PreferencesManager.esp32CamUrl = url
    }

    fun setOllamaUrl(url: String) {
        _ollamaUrl.value = url
        PreferencesManager.ollamaUrl = url
    }

    fun setOllamaModel(model: String) {
        _ollamaModel.value = model
        PreferencesManager.ollamaModel = model
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
