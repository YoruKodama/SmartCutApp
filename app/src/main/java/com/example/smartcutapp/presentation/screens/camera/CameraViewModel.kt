package com.example.smartcutapp.presentation.screens.camera

import android.util.Base64
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
        val ollamaUrl = PreferencesManager.ollamaUrl.trimEnd('/')
        val ollamaModel = PreferencesManager.ollamaModel.ifBlank { "llava" }

        if (ollamaUrl.isBlank()) {
            _error.value = "Укажите адрес Ollama в Настройках → AI Помощник"
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

                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                val response: OllamaResponse = client.post("$ollamaUrl/api/generate") {
                    contentType(ContentType.Application.Json)
                    setBody(OllamaRequest(
                        model = ollamaModel,
                        prompt = "Describe what you see in this image briefly in one sentence.",
                        images = listOf(base64Image)
                    ))
                }.body()

                _result.value = response.response.trim().ifEmpty { "Не удалось описать изображение" }

            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("Connection refused") == true ||
                    e.message?.contains("ConnectException") == true ->
                        "Нет подключения. Проверьте адреса ESP32-CAM и Ollama в настройках."
                    e.message?.contains("SocketTimeoutException") == true ->
                        "Время ожидания истекло. Ollama может долго загружать модель."
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
