package com.example.smartcutapp.data.local

import android.content.Context

object PreferencesManager {

    private const val PREFS_NAME = "smartcut_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_MQTT_BROKER = "mqtt_broker"

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
}
