package com.example.smartcutapp.data

/** Внутренние параметры сервисов. Пользователю не показываются; меняются только здесь, в коде. */
object AppConfig {
    /** Адрес Ollama (компьютер в локальной сети; для эмулятора — http://10.0.2.2:11434). */
    const val OLLAMA_URL = "http://192.168.1.2:11434"

    /** Модель распознавания продукта по фото. */
    const val OLLAMA_VISION_MODEL = "llava"

    /** Текстовая модель: калории, рацион, чат. */
    const val OLLAMA_TEXT_MODEL = "qwen2.5:7b"

    /** Необязательный облачный Mistral; пусто — всё идёт через Ollama. */
    const val MISTRAL_API_KEY = ""

    const val MQTT_BROKER_URL = "tcp://broker.hivemq.com:1883"
}
