package com.example.smartcutapp.presentation.screens.product_scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.ai.LlmClient
import com.example.smartcutapp.data.ai.NutritionQuery
import com.example.smartcutapp.data.ai.NutritionRepository
import com.example.smartcutapp.data.ai.ProductRecognizer
import com.example.smartcutapp.data.mqtt.MqttManager
import com.example.smartcutapp.domain.model.CutType
import com.example.smartcutapp.domain.model.ProductNutrition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CuttingPreset(
    val productNameRu: String,
    val emoji: String,
    val cut: CutType,
    val speed: Float
)

class ProductScanViewModel : ViewModel() {

    val isConnected = MqttManager.isConnected

    private val _capturedImage = MutableStateFlow<ByteArray?>(null)
    val capturedImage: StateFlow<ByteArray?> = _capturedImage

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    private val _rawCaption = MutableStateFlow<String?>(null)
    val rawCaption: StateFlow<String?> = _rawCaption

    /** Название распознанного продукта по-русски. */
    private val _nameRu = MutableStateFlow<String?>(null)
    val nameRu: StateFlow<String?> = _nameRu

    private val _detectedPreset = MutableStateFlow<CuttingPreset?>(null)
    val detectedPreset: StateFlow<CuttingPreset?> = _detectedPreset

    private val _speed = MutableStateFlow(0.5f)
    val speed: StateFlow<Float> = _speed

    private val _nutrition = MutableStateFlow<ProductNutrition?>(null)
    val nutrition: StateFlow<ProductNutrition?> = _nutrition

    private val _nutritionLoading = MutableStateFlow(false)
    val nutritionLoading: StateFlow<Boolean> = _nutritionLoading

    private val _nutritionError = MutableStateFlow<String?>(null)
    val nutritionError: StateFlow<String?> = _nutritionError

    private val _grams = MutableStateFlow(100)
    val grams: StateFlow<Int> = _grams

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _sendResult = MutableStateFlow<String?>(null)
    val sendResult: StateFlow<String?> = _sendResult

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    /** Фото с камеры телефона или из галереи → vision-модель в Ollama. */
    fun analyze(context: Context, uri: Uri) {
        val appContext = context.applicationContext
        _isAnalyzing.value = true
        _error.value = null
        _detectedPreset.value = null
        _rawCaption.value = null
        _nameRu.value = null
        _capturedImage.value = null
        _nutrition.value = null
        _nutritionError.value = null

        viewModelScope.launch {
            try {
                val imageBytes = ProductRecognizer.loadImage(appContext, uri)
                _capturedImage.value = imageBytes

                val caption = ProductRecognizer.identify(imageBytes)
                if (caption.isBlank()) error("Нейросеть не смогла распознать продукт")
                _rawCaption.value = caption

                val preset = detectPreset(caption)
                _detectedPreset.value = preset
                _nameRu.value = preset.productNameRu.takeUnless { preset === DEFAULT_PRESET }
                    ?: caption.replaceFirstChar { it.uppercase() }
                if (preset === DEFAULT_PRESET) translateName(caption)
                _speed.value = preset.speed
                calculateNutrition(caption)
            } catch (e: Exception) {
                _error.value = if (e.message?.contains("timeout", ignoreCase = true) == true)
                    "Время ожидания истекло. Сервис может запускаться (~30 сек). Попробуйте снова."
                else e.message ?: "Ошибка распознавания"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun setSpeed(v: Float) { _speed.value = v }

    fun setGrams(v: Int) { _grams.value = v.coerceIn(1, 5000) }

    /** КБЖУ распознанного продукта (нейросеть, на 100 г); граммы для итога пользователь задаёт сам. */
    private fun calculateNutrition(product: String) {
        viewModelScope.launch {
            _nutritionLoading.value = true
            _nutritionError.value = null
            try {
                _nutrition.value = NutritionRepository.estimate(listOf(NutritionQuery(product)))[0]
                    ?: error("Нейросеть не смогла оценить продукт")
            } catch (e: Exception) {
                _nutritionError.value = e.message ?: "Не удалось рассчитать КБЖУ"
            } finally {
                _nutritionLoading.value = false
            }
        }
    }

    /** Перевод названия продукта на русский (для продуктов, которых нет в списке готовых режимов). */
    private fun translateName(english: String) {
        viewModelScope.launch {
            val ru = runCatching {
                LlmClient.chat(
                    system = "Ты переводчик названий продуктов питания. Ответь ТОЛЬКО названием по-русски, " +
                        "одним-двумя словами, в именительном падеже, без пояснений и точки.",
                    user = english,
                    temperature = 0.0,
                    maxTokens = 20
                )
            }.getOrNull()?.trim('.', ' ', '"', '«', '»', '\n')?.takeIf { it.isNotBlank() && it.length < 40 }
            if (ru != null) _nameRu.value = ru.replaceFirstChar { it.uppercase() }
        }
    }

    fun retryNutrition() {
        _rawCaption.value?.takeIf { it.isNotBlank() }?.let { calculateNutrition(it) }
    }

    fun applyPreset() {
        val preset = _detectedPreset.value ?: return
        viewModelScope.launch {
            _isSending.value = true
            val payload = """{"mode":"${preset.cut.apiName}","speed":${"%.2f".format(_speed.value)}}"""
            val result = MqttManager.sendCommand(payload)
            _sendResult.value = if (result.isSuccess) "Режим применён к SlicerBot!"
            else "Ошибка отправки: ${result.exceptionOrNull()?.message}"
            _isSending.value = false
        }
    }

    fun clearSendResult() { _sendResult.value = null }

    fun clearAll() {
        _capturedImage.value = null
        _rawCaption.value = null
        _nameRu.value = null
        _detectedPreset.value = null
        _nutrition.value = null
        _nutritionError.value = null
        _grams.value = 100
        _error.value = null
        _sendResult.value = null
    }

    private fun detectPreset(caption: String): CuttingPreset {
        val lower = caption.lowercase().trim()
        return PRODUCT_PRESETS.firstOrNull { (keywords, _) ->
            keywords.any { it in lower }
        }?.second ?: DEFAULT_PRESET
    }

    companion object {
        private val PRODUCT_PRESETS: List<Pair<List<String>, CuttingPreset>> = listOf(
            listOf("carrot") to CuttingPreset("Морковь", "🥕", CutType.GRATE, 0.6f),
            listOf("beet", "beetroot") to CuttingPreset("Свёкла", "🫙", CutType.GRATE, 0.6f),
            listOf("cheese") to CuttingPreset("Сыр", "🧀", CutType.GRATE, 0.5f),
            listOf("apple") to CuttingPreset("Яблоко", "🍎", CutType.GRATE, 0.4f),
            listOf("cabbage") to CuttingPreset("Капуста", "🥬", CutType.SHRED, 0.6f),
            listOf("onion") to CuttingPreset("Лук", "🧅", CutType.SHRED, 0.5f),
            listOf("potato") to CuttingPreset("Картофель", "🥔", CutType.STICKS, 0.5f),
            listOf("cucumber") to CuttingPreset("Огурец", "🥒", CutType.STICKS, 0.4f),
            listOf("zucchini", "courgette") to CuttingPreset("Цуккини", "🥬", CutType.STICKS, 0.4f),
            listOf("eggplant", "aubergine") to CuttingPreset("Баклажан", "🍆", CutType.STICKS, 0.4f),
            listOf("pepper", "bell pepper", "capsicum") to CuttingPreset("Перец", "🌶", CutType.STICKS, 0.5f),
            listOf("celery") to CuttingPreset("Сельдерей", "🌿", CutType.STICKS, 0.6f),
        )

        private val DEFAULT_PRESET = CuttingPreset(
            productNameRu = "Продукт",
            emoji = "🔪",
            cut = CutType.STICKS,
            speed = 0.5f
        )
    }
}
