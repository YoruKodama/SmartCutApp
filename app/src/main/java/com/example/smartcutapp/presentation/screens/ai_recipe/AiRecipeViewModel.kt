package com.example.smartcutapp.presentation.screens.ai_recipe

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

data class ChatMessage(val role: String, val content: String)

@Serializable
private data class HFChatRequest(
    val model: String,
    val messages: List<HFMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 1024
)

@Serializable
private data class HFMessage(val role: String, val content: String)

@Serializable
private data class HFChatResponse(val choices: List<HFChoice>)

@Serializable
private data class HFChoice(val message: HFMessage)

class AiRecipeViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val systemPrompt = "Ты кулинарный помощник для приложения SmartCut. Помогай составлять рецепты, предлагай ингредиенты и пошаговые инструкции. Структурируй рецепты: название, ингредиенты с количеством, пошаговое приготовление. Если пользователь перечисляет ингредиенты — предлагай блюда из них. Отвечай только на русском языке."

    fun sendMessage(text: String) {
        val apiKey = PreferencesManager.mistralApiKey
        if (apiKey.isBlank()) {
            _error.value = "Укажите Mistral API ключ в Настройках → AI Помощник"
            return
        }
        if (text.isBlank()) return

        _messages.value = _messages.value + ChatMessage("user", text)
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val history = _messages.value.map { HFMessage(it.role, it.content) }

                val response: HFChatResponse = client.post(
                    "https://api.mistral.ai/v1/chat/completions"
                ) {
                    header("Authorization", "Bearer $apiKey")
                    contentType(ContentType.Application.Json)
                    setBody(HFChatRequest(
                        model = "mistral-small-latest",
                        messages = listOf(HFMessage("system", systemPrompt)) + history
                    ))
                }.body()

                val reply = response.choices.firstOrNull()?.message?.content ?: "Нет ответа"
                _messages.value = _messages.value + ChatMessage("assistant", reply)
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
                _messages.value = _messages.value.dropLast(1)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
