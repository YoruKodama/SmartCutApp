package com.example.smartcutapp.data.repository

import com.example.smartcutapp.data.remote.api.ApiClient
import com.example.smartcutapp.data.remote.dto.AuthResponseDto
import com.example.smartcutapp.data.remote.dto.LoginRequestDto
import com.example.smartcutapp.data.remote.dto.RegisterRequestDto
import com.example.smartcutapp.domain.model.User
import com.example.smartcutapp.domain.repository.AuthRepository
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthRepositoryImpl : AuthRepository {

    private var token: String? = null

    override suspend fun register(name: String, email: String, password: String): User {
        val response = ApiClient.client.post("${ApiClient.BASE_URL}/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(name, email, password))
        }
        val dto = response.body<AuthResponseDto>()
        token = dto.token
        return User(dto.userId, dto.name, dto.token)
    }

    override suspend fun login(email: String, password: String): User {
        val response = ApiClient.client.post("${ApiClient.BASE_URL}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email, password))
        }
        val dto = response.body<AuthResponseDto>()
        token = dto.token
        return User(dto.userId, dto.name, dto.token)
    }

    override suspend fun logout() {
        token = null
    }

    override suspend fun getToken(): String? = token
}