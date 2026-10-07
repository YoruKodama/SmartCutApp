package com.example.smartcutapp.data.mqtt

import com.example.smartcutapp.data.AppConfig
import com.example.smartcutapp.data.local.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

object MqttManager {

    /** Топики без привязки к устройству (по умолчанию, как раньше). */
    const val TOPIC_COMMAND = "smartcut/cmd"
    const val TOPIC_STATUS = "smartcut/status"
    const val TOPIC_WEIGHT = "smartcut/weight"

    /** С привязанным устройством топики получают вид smartcut/<deviceId>/cmd и т.д. */
    val commandTopic: String get() = topic("cmd")
    val statusTopic: String get() = topic("status")
    val weightTopic: String get() = topic("weight")

    private fun topic(suffix: String): String {
        val id = PreferencesManager.deviceId.trim().trim('/')
        return if (id.isEmpty()) "smartcut/$suffix" else "smartcut/$id/$suffix"
    }

    private var client: MqttClient? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _deviceStatus = MutableStateFlow("")
    val deviceStatus: StateFlow<String> = _deviceStatus

    private val _esp32Online = MutableStateFlow(false)
    val esp32Online: StateFlow<Boolean> = _esp32Online

    private val _weightGrams = MutableStateFlow(0.0f)
    val weightGrams: StateFlow<Float> = _weightGrams

    /**
     * true, если связь была и оборвалась (идёт переподключение). Пока true, новые команды не
     * отправляются; цикл, который уже идёт, устройство доделывает само.
     */
    private val _linkLost = MutableStateFlow(false)
    val linkLost: StateFlow<Boolean> = _linkLost

    private val brokerUrl: String get() = AppConfig.MQTT_BROKER_URL

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
                isAutomaticReconnect = true
                maxReconnectDelay = 5000 // повторные попытки не реже чем раз в 5 секунд
            }
            mqttClient.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    if (reconnect) {
                        // clean session: подписки после обрыва нужно восстановить
                        runCatching { subscribeAll(mqttClient) }
                        _isConnected.value = true
                        _linkLost.value = false
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    _isConnected.value = false
                    _esp32Online.value = false
                    _linkLost.value = true
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: ""
                    _deviceStatus.value = payload
                    when (topic) {
                        statusTopic -> _esp32Online.value = true
                        weightTopic -> {
                            val weight = parseWeight(payload)
                            if (weight != null) _weightGrams.value = weight
                        }
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })
            mqttClient.connect(opts)
            subscribeAll(mqttClient)
            client = mqttClient
            _isConnected.value = true
            _linkLost.value = false
        }
    }

    private fun subscribeAll(c: MqttClient) {
        c.subscribe(statusTopic)
        c.subscribe(weightTopic)
    }

    suspend fun publish(topic: String, payload: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val c = client ?: error("Нет связи с устройством")
            if (!c.isConnected) error("Нет связи с устройством")
            val msg = MqttMessage(payload.toByteArray()).apply { qos = 1 }
            c.publish(topic, msg)
        }
    }

    /** Команда на устройство. Не отправляется, пока связь потеряна. */
    suspend fun sendCommand(payload: String): Result<Unit> {
        if (_linkLost.value) {
            return Result.failure(IllegalStateException("Связь с устройством потеряна — команды не отправляются"))
        }
        return publish(commandTopic, payload)
    }

    /** «Стоп» — всегда пробуем отправить, независимо от режима экрана. */
    suspend fun sendStop(): Result<Unit> = publish(commandTopic, DeviceCommands.stop())

    fun disconnect() {
        runCatching { client?.disconnect() }
        client = null
        _isConnected.value = false
        _esp32Online.value = false
        _linkLost.value = false
    }

    // Parses {"weight": 245.5} or plain "245.5"
    private fun parseWeight(payload: String): Float? {
        return try {
            val json = payload.trim()
            if (json.startsWith("{")) {
                val match = Regex(""""weight"\s*:\s*([\d.]+)""").find(json)
                match?.groupValues?.get(1)?.toFloatOrNull()
            } else {
                json.toFloatOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
}
