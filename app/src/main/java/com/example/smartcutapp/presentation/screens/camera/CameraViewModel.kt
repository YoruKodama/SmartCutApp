package com.example.smartcutapp.presentation.screens.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.local.PreferencesManager
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class HFCaptionResult(
    @SerialName("generated_text") val generatedText: String
)

class CameraViewModel : ViewModel() {

    private val _capturedImage = MutableStateFlow<ByteArray?>(null)
    val capturedImage: StateFlow<ByteArray?> = _capturedImage

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    fun captureAndAnalyze() {
        val camUrl = PreferencesManager.esp32CamUrl.trimEnd('/')
        val hfToken = PreferencesManager.hfToken

        if (hfToken.isBlank()) {
            _error.value = "Укажите HuggingFace токен в Настройках → AI Помощник"
            return
        }

        _isCapturing.value = true
        _result.value = null
        _error.value = null

        viewModelScope.launch {
            try {
                val imageBytes: ByteArray = client.get("$camUrl/capture").body()
                _capturedImage.value = imageBytes
                _isCapturing.value = false
                _isAnalyzing.value = true

                val results: List<HFCaptionResult> = client.post(
                    "https://api-inference.huggingface.co/models/Salesforce/blip-image-captioning-large"
                ) {
                    header("Authorization", "Bearer $hfToken")
                    contentType(ContentType.Application.OctetStream)
                    setBody(imageBytes)
                }.body()

                val caption = results.firstOrNull()?.generatedText ?: "Не распознано"
                _result.value = caption

            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("Connection refused") == true ||
                    e.message?.contains("ConnectException") == true ||
                    e.message?.contains("SocketTimeoutException") == true ->
                        "ESP32-CAM недоступна. Проверьте IP-адрес в настройках и подключение к Wi-Fi."
                    e.message?.contains("Loading") == true ->
                        "Модель загружается на сервере HuggingFace (~20 сек), попробуйте ещё раз."
                    else -> "Ошибка: ${e.message}"
                }
            } finally {
                _isCapturing.value = false
                _isAnalyzing.value = false
            }
        }
    }

    fun clearResult() {
        _result.value = null
        _error.value = null
        _capturedImage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
