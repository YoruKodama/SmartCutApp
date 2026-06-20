package com.example.smartcutapp.data.local

import android.content.Context

object PreferencesManager {

    private const val PREFS_NAME = "smartcut_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_MQTT_BROKER = "mqtt_broker"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_CLAUDE_API_KEY = "claude_api_key"
    private const val KEY_MISTRAL_API_KEY = "mistral_api_key"
    private const val KEY_ESP32_CAM_URL = "esp32_cam_url"
    private const val KEY_OLLAMA_URL = "ollama_url"
    private const val KEY_OLLAMA_MODEL = "ollama_model"

    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context.applicationContext
    }

    private val prefs get() = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var token: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var mqttBrokerUrl: String
        get() = prefs.getString(KEY_MQTT_BROKER, "tcp://broker.hivemq.com:1883") ?: "tcp://broker.hivemq.com:1883"
        set(value) = prefs.edit().putString(KEY_MQTT_BROKER, value).apply()

    var themeMode: Int
        get() = prefs.getInt(KEY_THEME_MODE, 0)
        set(value) = prefs.edit().putInt(KEY_THEME_MODE, value).apply()

    var claudeApiKey: String
        get() = prefs.getString(KEY_CLAUDE_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CLAUDE_API_KEY, value).apply()

    var mistralApiKey: String
        get() = prefs.getString(KEY_MISTRAL_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MISTRAL_API_KEY, value).apply()

    var esp32CamUrl: String
        get() = prefs.getString(KEY_ESP32_CAM_URL, "http://192.168.4.1") ?: "http://192.168.4.1"
        set(value) = prefs.edit().putString(KEY_ESP32_CAM_URL, value).apply()

    var ollamaUrl: String
        get() = prefs.getString(KEY_OLLAMA_URL, "http://192.168.1.100:11434") ?: "http://192.168.1.100:11434"
        set(value) = prefs.edit().putString(KEY_OLLAMA_URL, value).apply()

    var ollamaModel: String
        get() = prefs.getString(KEY_OLLAMA_MODEL, "llava") ?: "llava"
        set(value) = prefs.edit().putString(KEY_OLLAMA_MODEL, value).apply()
}
