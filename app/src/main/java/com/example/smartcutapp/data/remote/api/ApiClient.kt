package com.example.smartcutapp.data.remote.api

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json

object ApiClient {

    private const val DEVICE_HOST = "192.168.1.5" // IP компьютера с бэком в локальной сети (для реального телефона)
    private const val EMULATOR_HOST = "10.0.2.2"  // так эмулятор видит localhost компьютера
    private const val PORT = 8081

    private val isEmulator: Boolean
        get() = android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.contains("emulator") ||
            android.os.Build.MODEL.contains("sdk_gphone") ||
            android.os.Build.MODEL.contains("Emulator")

    val BASE_URL: String
        get() = "http://${if (isEmulator) EMULATOR_HOST else DEVICE_HOST}:$PORT"

    /**
     * Бэк хранит адреса картинок с собственным хостом из конфига (например 192.168.1.5), который
     * может быть недоступен с эмулятора. Подменяем хост на актуальный только при показе.
     */
    fun resolveImageUrl(url: String?): String? {
        if (url.isNullOrEmpty()) return url
        val path = url.substringAfter("://", "").substringAfter('/', "")
        return if (url.startsWith("http") && path.isNotEmpty()) "$BASE_URL/$path" else url
    }

    /** Срабатывает, когда бэк отклонил токен (401): токен сброшен, нужно войти заново. */
    val sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    class SessionExpiredException : Exception("Сессия истекла. Войдите в аккаунт заново")

    val client = HttpClient(Android) {
        HttpResponseValidator {
            validateResponse { response ->
                // 401 на /auth/* — это неверный логин или пароль, а не протухший токен
                if (response.status == HttpStatusCode.Unauthorized &&
                    !response.request.url.encodedPath.startsWith("/auth")
                ) {
                    TokenStorage.token = ""
                    sessionExpired.tryEmit(Unit)
                    throw SessionExpiredException()
                }
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }
}