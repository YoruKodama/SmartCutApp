package com.example.smartcutapp.data.ai

import com.example.smartcutapp.data.AppConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class LlmMessage(val role: String, val content: String)

@Serializable
private data class Msg(val role: String, val content: String)

@Serializable
private data class MistralRequest(
    val model: String,
    val messages: List<Msg>,
    val temperature: Double,
    @SerialName("max_tokens") val maxTokens: Int,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null
)

@Serializable
private data class ResponseFormat(val type: String)

@Serializable
private data class MistralResponse(val choices: List<Choice> = emptyList())

@Serializable
private data class Choice(val message: Msg)

@Serializable
private data class OllamaOptions(
    val temperature: Double,
    @SerialName("num_predict") val numPredict: Int
)

@Serializable
private data class OllamaChatRequest(
    val model: String,
    val messages: List<Msg>,
    val stream: Boolean = false,
    val format: String? = null,
    val options: OllamaOptions
)

@Serializable
private data class OllamaChatResponse(val message: Msg? = null)

/**
 * Текстовая нейросеть для калорий, рациона и чата. Сначала облачный Mistral; если ключа нет
 * или токены закончились (401/402/429), недоступна сеть — автоматически локальная модель в Ollama.
 */
object LlmClient {

    private const val MISTRAL_URL = "https://api.mistral.ai/v1/chat/completions"
    private const val MISTRAL_MODEL = "mistral-small-latest"

    private val lenientJson = Json { ignoreUnknownKeys = true }

    private val http = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) {
            requestTimeoutMillis = 180_000 // локальная модель на CPU может отвечать долго
            connectTimeoutMillis = 15_000
        }
    }

    suspend fun chat(
        system: String,
        user: String,
        json: Boolean = false,
        temperature: Double = 0.2,
        maxTokens: Int = 1500
    ): String = chat(system, listOf(LlmMessage("user", user)), json, temperature, maxTokens)

    /** Ответ модели. [json] — попросить строго JSON-объект. Исключение — с текстом для пользователя. */
    suspend fun chat(
        system: String,
        messages: List<LlmMessage>,
        json: Boolean = false,
        temperature: Double = 0.2,
        maxTokens: Int = 1500
    ): String {
        val all = listOf(Msg("system", system)) + messages.map { Msg(it.role, it.content) }

        val mistralError: String? = if (AppConfig.MISTRAL_API_KEY.isBlank()) null
        else try {
            return mistral(all, json, temperature, maxTokens)
        } catch (e: Exception) {
            e.message ?: "ошибка"
        }

        return try {
            ollama(all, json, temperature, maxTokens)
        } catch (e: Exception) {
            val local = e.message ?: "ошибка"
            error(
                if (mistralError == null) "ИИ-сервис недоступен: $local"
                else "ИИ-сервис недоступен. Попробуйте позже ($local)"
            )
        }
    }

    private suspend fun mistral(msgs: List<Msg>, json: Boolean, temperature: Double, maxTokens: Int): String {
        val response = http.post(MISTRAL_URL) {
            header(HttpHeaders.Authorization, "Bearer ${AppConfig.MISTRAL_API_KEY}")
            contentType(ContentType.Application.Json)
            setBody(
                MistralRequest(
                    model = MISTRAL_MODEL,
                    messages = msgs,
                    temperature = temperature,
                    maxTokens = maxTokens,
                    responseFormat = if (json) ResponseFormat("json_object") else null
                )
            )
        }
        if (!response.status.isSuccess()) {
            error(
                when (response.status.value) {
                    401 -> "неверный ключ"
                    402, 429 -> "лимит токенов исчерпан"
                    else -> "HTTP ${response.status.value}"
                }
            )
        }
        return response.body<MistralResponse>().choices.firstOrNull()?.message?.content?.trim()
            ?.takeIf { it.isNotEmpty() } ?: error("пустой ответ")
    }

    private suspend fun ollama(msgs: List<Msg>, json: Boolean, temperature: Double, maxTokens: Int): String {
        val base = AppConfig.OLLAMA_URL.trimEnd('/')
        if (base.isBlank()) error("сервис не настроен")
        val response = http.post("$base/api/chat") {
            contentType(ContentType.Application.Json)
            setBody(
                OllamaChatRequest(
                    model = AppConfig.OLLAMA_TEXT_MODEL.ifBlank { "mistral" },
                    messages = msgs,
                    format = if (json) "json" else null,
                    options = OllamaOptions(temperature, maxTokens)
                )
            )
        }
        if (!response.status.isSuccess()) {
            error(
                if (response.status.value == 404) "модель не установлена на сервере"
                else "ошибка сервера ${response.status.value}"
            )
        }
        // Ollama может отдавать application/x-ndjson: разбираем построчно и склеиваем куски ответа
        return response.bodyAsText().lineSequence()
            .filter { it.isNotBlank() }
            .joinToString("") { lenientJson.decodeFromString<OllamaChatResponse>(it).message?.content.orEmpty() }
            .trim()
            .takeIf { it.isNotEmpty() } ?: error("пустой ответ")
    }
}
