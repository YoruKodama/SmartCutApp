package com.example.smartcutapp.presentation.screens.product_scan

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.data.mqtt.MqttManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class CuttingPreset(
    val productNameRu: String,
    val emoji: String,
    val mode: String,
    val thickness: Int,
    val cubeSize: Int,
    val speed: Float,
    val modeLabel: String
)

@Serializable
private data class OllamaRequest(
    val model: String,
    val prompt: String,
    val images: List<String>,
    val stream: Boolean = false
)

@Serializable
private data class OllamaResponse(
    val response: String = "",
    val done: Boolean = false
)

class ProductScanViewModel : ViewModel() {

    val isConnected = MqttManager.isConnected

    private val _capturedImage = MutableStateFlow<ByteArray?>(null)
    val capturedImage: StateFlow<ByteArray?> = _capturedImage

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    private val _rawCaption = MutableStateFlow<String?>(null)
    val rawCaption: StateFlow<String?> = _rawCaption

    private val _detectedPreset = MutableStateFlow<CuttingPreset?>(null)
    val detectedPreset: StateFlow<CuttingPreset?> = _detectedPreset

    private val _thickness = MutableStateFlow(10)
    val thickness: StateFlow<Int> = _thickness

    private val _cubeSize = MutableStateFlow(15)
    val cubeSize: StateFlow<Int> = _cubeSize

    private val _speed = MutableStateFlow(0.5f)
    val speed: StateFlow<Float> = _speed

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _sendResult = MutableStateFlow<String?>(null)
    val sendResult: StateFlow<String?> = _sendResult

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    fun scan() {
        val camUrl = PreferencesManager.esp32CamUrl.trimEnd('/')
        val ollamaUrl = PreferencesManager.ollamaUrl.trimEnd('/')
        val ollamaModel = PreferencesManager.ollamaModel.ifBlank { "llava" }

        if (ollamaUrl.isBlank()) {
            _error.value = "Укажите адрес Ollama в Настройках → AI Помощник"
            return
        }
        if (camUrl.isBlank()) {
            _error.value = "Укажите URL ESP32-CAM в Настройках → ESP32 КАМЕРА"
            return
        }

        _isCapturing.value = true
        _error.value = null
        _detectedPreset.value = null
        _rawCaption.value = null
        _capturedImage.value = null

        viewModelScope.launch {
            try {
                val imageBytes: ByteArray = client.get("$camUrl/capture").body()
                _capturedImage.value = imageBytes
                _isCapturing.value = false
                _isAnalyzing.value = true

                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                val response: OllamaResponse = client.post("$ollamaUrl/api/generate") {
                    contentType(ContentType.Application.Json)
                    setBody(OllamaRequest(
                        model = ollamaModel,
                        prompt = "What single food item, vegetable, or fruit is in this image? " +
                                "Respond with ONLY the food name in English, one or two words maximum. " +
                                "Examples: tomato, cucumber, potato, bread, carrot.",
                        images = listOf(base64Image)
                    ))
                }.body()

                val caption = response.response.trim()
                _rawCaption.value = caption

                val preset = detectPreset(caption)
                _detectedPreset.value = preset
                _thickness.value = preset.thickness
                _cubeSize.value = preset.cubeSize
                _speed.value = preset.speed

            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("Connection refused") == true ||
                    e.message?.contains("ConnectException") == true ->
                        "Нет подключения. Проверьте IP ESP32-CAM и адрес Ollama в настройках."
                    e.message?.contains("SocketTimeoutException") == true ->
                        "Время ожидания истекло. Ollama может загружать модель (~30 сек). Попробуйте снова."
                    else -> "Ошибка: ${e.message}"
                }
            } finally {
                _isCapturing.value = false
                _isAnalyzing.value = false
            }
        }
    }

    fun setThickness(v: Int) { _thickness.value = v.coerceIn(1, 50) }
    fun setCubeSize(v: Int) { _cubeSize.value = v.coerceIn(5, 50) }
    fun setSpeed(v: Float) { _speed.value = v }

    fun applyPreset() {
        val preset = _detectedPreset.value ?: return
        viewModelScope.launch {
            _isSending.value = true
            val payload = if (preset.mode == "cube") {
                """{"mode":"cube","width":${_cubeSize.value},"height":${_cubeSize.value},"speed":${"%.2f".format(_speed.value)}}"""
            } else {
                """{"mode":"slice","thickness":${_thickness.value},"speed":${"%.2f".format(_speed.value)}}"""
            }
            val result = MqttManager.publish(MqttManager.TOPIC_COMMAND, payload)
            _sendResult.value = if (result.isSuccess) "Режим применён к SlicerBot!"
            else "Ошибка отправки: ${result.exceptionOrNull()?.message}"
            _isSending.value = false
        }
    }

    fun clearSendResult() { _sendResult.value = null }

    fun clearAll() {
        _capturedImage.value = null
        _rawCaption.value = null
        _detectedPreset.value = null
        _error.value = null
        _sendResult.value = null
    }

    private fun detectPreset(caption: String): CuttingPreset {
        val lower = caption.lowercase().trim()
        return PRODUCT_PRESETS.firstOrNull { (keywords, _) ->
            keywords.any { it in lower }
        }?.second ?: DEFAULT_PRESET
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }

    companion object {
        private val PRODUCT_PRESETS: List<Pair<List<String>, CuttingPreset>> = listOf(
            listOf("tomato") to CuttingPreset("Помидор", "🍅", "slice", 5, 0, 0.3f, "Слайсами"),
            listOf("cucumber") to CuttingPreset("Огурец", "🥒", "slice", 7, 0, 0.4f, "Слайсами"),
            listOf("banana") to CuttingPreset("Банан", "🍌", "slice", 10, 0, 0.3f, "Слайсами"),
            listOf("apple") to CuttingPreset("Яблоко", "🍎", "slice", 8, 0, 0.4f, "Слайсами"),
            listOf("carrot") to CuttingPreset("Морковь", "🥕", "slice", 3, 0, 0.6f, "Слайсами"),
            listOf("potato") to CuttingPreset("Картофель", "🥔", "cube", 0, 15, 0.5f, "Кубиками"),
            listOf("bread", "toast", "loaf", "bun") to CuttingPreset("Хлеб", "🍞", "slice", 15, 0, 0.4f, "Слайсами"),
            listOf("onion") to CuttingPreset("Лук", "🧅", "slice", 4, 0, 0.5f, "Слайсами"),
            listOf("lemon") to CuttingPreset("Лимон", "🍋", "slice", 5, 0, 0.5f, "Слайсами"),
            listOf("orange") to CuttingPreset("Апельсин", "🍊", "slice", 8, 0, 0.4f, "Слайсами"),
            listOf("chicken", "steak", "pork", "beef", "meat") to CuttingPreset("Мясо", "🥩", "slice", 5, 0, 0.5f, "Слайсами"),
            listOf("pepper", "bell pepper", "capsicum") to CuttingPreset("Перец", "🌶", "slice", 7, 0, 0.5f, "Слайсами"),
            listOf("zucchini", "courgette") to CuttingPreset("Цуккини", "🥬", "slice", 8, 0, 0.4f, "Слайсами"),
            listOf("mushroom") to CuttingPreset("Грибы", "🍄", "slice", 4, 0, 0.5f, "Слайсами"),
            listOf("cheese") to CuttingPreset("Сыр", "🧀", "slice", 5, 0, 0.5f, "Слайсами"),
            listOf("eggplant", "aubergine") to CuttingPreset("Баклажан", "🍆", "slice", 8, 0, 0.4f, "Слайсами"),
            listOf("cabbage") to CuttingPreset("Капуста", "🥬", "slice", 3, 0, 0.6f, "Слайсами"),
            listOf("strawberry") to CuttingPreset("Клубника", "🍓", "slice", 5, 0, 0.3f, "Слайсами"),
            listOf("watermelon", "melon") to CuttingPreset("Арбуз/Дыня", "🍉", "slice", 20, 0, 0.4f, "Слайсами"),
            listOf("kiwi") to CuttingPreset("Киви", "🥝", "slice", 8, 0, 0.4f, "Слайсами"),
            listOf("avocado") to CuttingPreset("Авокадо", "🥑", "slice", 10, 0, 0.3f, "Слайсами"),
            listOf("pineapple") to CuttingPreset("Ананас", "🍍", "slice", 12, 0, 0.4f, "Слайсами"),
            listOf("mango") to CuttingPreset("Манго", "🥭", "slice", 10, 0, 0.4f, "Слайсами"),
            listOf("celery") to CuttingPreset("Сельдерей", "🌿", "slice", 5, 0, 0.6f, "Слайсами"),
            listOf("beet", "beetroot") to CuttingPreset("Свёкла", "🫙", "slice", 4, 0, 0.6f, "Слайсами"),
        )

        private val DEFAULT_PRESET = CuttingPreset(
            productNameRu = "Продукт",
            emoji = "🔪",
            mode = "slice",
            thickness = 10,
            cubeSize = 0,
            speed = 0.5f,
            modeLabel = "Слайсами"
        )
    }
}
