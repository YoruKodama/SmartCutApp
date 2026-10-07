package com.example.smartcutapp.presentation.screens.ai_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcutapp.data.ai.LlmClient
import com.example.smartcutapp.data.ai.LlmMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val content: String)

class AiRecipeViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val systemPrompt = "Ты кулинарный помощник для приложения SmartCut. Помогай составлять рецепты, предлагай ингредиенты и пошаговые инструкции. Структурируй рецепты: название, ингредиенты с количеством, пошаговое приготовление. Если пользователь перечисляет ингредиенты — предлагай блюда из них. Отвечай только на русском языке."

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _messages.value = _messages.value + ChatMessage("user", text)
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val history = _messages.value.map { LlmMessage(it.role, it.content) }
                val reply = LlmClient.chat(systemPrompt, history, temperature = 0.7, maxTokens = 1024)
                _messages.value = _messages.value + ChatMessage("assistant", reply)
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка"
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
}
