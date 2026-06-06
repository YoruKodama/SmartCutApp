package com.example.smartcutapp.data.mqtt

import com.example.smartcutapp.data.local.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

object MqttManager {

    const val TOPIC_COMMAND = "smartcut/cmd"
    const val TOPIC_STATUS = "smartcut/status"

    private var client: MqttClient? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _deviceStatus = MutableStateFlow("")
    val deviceStatus: StateFlow<String> = _deviceStatus

    private val _esp32Online = MutableStateFlow(false)
    val esp32Online: StateFlow<Boolean> = _esp32Online

    var brokerUrl: String
        get() = PreferencesManager.mqttBrokerUrl
        set(value) { PreferencesManager.mqttBrokerUrl = value }

    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            disconnect()
            val mqttClient = MqttClient(
                brokerUrl,
                "SmartCutApp-${System.currentTimeMillis()}",
                MemoryPersistence()
            )
            val opts = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 30
            }
            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    _isConnected.value = false
                    _esp32Online.value = false
                }
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    _deviceStatus.value = message?.toString() ?: ""
                    if (topic == TOPIC_STATUS) {
                        _esp32Online.value = true
                    }
                }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })
            mqttClient.connect(opts)
            mqttClient.subscribe(TOPIC_STATUS)
            client = mqttClient
            _isConnected.value = true
        }
    }

    suspend fun publish(topic: String, payload: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val c = client ?: error("Нет подключения к брокеру")
            if (!c.isConnected) error("Нет подключения к брокеру")
            val msg = MqttMessage(payload.toByteArray()).apply { qos = 1 }
            c.publish(topic, msg)
        }
    }

    fun disconnect() {
        runCatching { client?.disconnect() }
        client = null
        _isConnected.value = false
        _esp32Online.value = false
    }
}
