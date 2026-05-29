package com.example.smartcutapp.domain.repository

import com.example.smartcutapp.domain.model.User

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): User
    suspend fun login(email: String, password: String): User
    suspend fun logout()
    suspend fun getToken(): String?
}