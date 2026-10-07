package com.example.smartcutapp.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

@Serializable
private data class GenerateRequest(
    val model: String,
    val prompt: String,
    val images: List<String>,
    val stream: Boolean = false
)

@Serializable
private data class GenerateResponse(val response: String = "")

/** Распознавание продукта по фото с телефона через готовую vision-модель в Ollama (локальная сеть, без облака). */
object ProductRecognizer {

    private const val MAX_SIDE = 1024

    private val lenientJson = Json { ignoreUnknownKeys = true }

    private val http = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000 // первая загрузка модели в Ollama может занять ~30 с
            connectTimeoutMillis = 10_000
        }
    }

    /** Фото из камеры/галереи → JPEG до 1024 px по большей стороне, с учётом поворота из EXIF. */
    suspend fun loadImage(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2

        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: error("Не удалось открыть изображение")

        val rotation = runCatching {
            resolver.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            }
        }.getOrNull() ?: 0f

        val bitmap = if (rotation == 0f) decoded
        else Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, Matrix().apply { postRotate(rotation) }, true)

        ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
            it.toByteArray()
        }
    }

    /** Название продукта на английском (одно-два слова). */
    suspend fun identify(image: ByteArray): String = ask(
        image,
        "What single food item, vegetable, or fruit is in this image? " +
            "Respond with ONLY the food name in English, one or two words maximum. " +
            "Examples: tomato, cucumber, potato, bread, carrot."
    ).lowercase().trim('.', ' ', '\n')

    /** Тот ли это продукт: true/false, null — модель ответила невнятно. */
    suspend fun isProduct(image: ByteArray, productName: String): Boolean? {
        val answer = ask(
            image,
            "Is there \"$productName\" (a food product) in this image? Answer with ONLY one word: yes or no."
        ).lowercase()
        return when {
            answer.startsWith("yes") || answer.startsWith("да") -> true
            answer.startsWith("no") || answer.startsWith("нет") -> false
            else -> null
        }
    }

    private suspend fun ask(image: ByteArray, prompt: String): String {
        val ollama = AppConfig.OLLAMA_URL.trimEnd('/')
        if (ollama.isBlank()) error("Сервис распознавания не настроен")
        val model = AppConfig.OLLAMA_VISION_MODEL.ifBlank { "llava" }
        val response = try {
            http.post("$ollama/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(GenerateRequest(model, prompt, listOf(Base64.encodeToString(image, Base64.NO_WRAP))))
            }
        } catch (e: Exception) {
            error("Сервис распознавания недоступен. Проверьте подключение к сети")
        }
        if (response.status.value == 404) error("Модель распознавания не установлена на сервере")
        if (!response.status.isSuccess()) error("Ошибка сервиса распознавания: ${response.status.value}")
        // Ollama может отдавать application/x-ndjson: разбираем построчно и склеиваем куски ответа
        return response.bodyAsText().lineSequence()
            .filter { it.isNotBlank() }
            .joinToString("") { lenientJson.decodeFromString<GenerateResponse>(it).response }
            .trim()
    }
}
