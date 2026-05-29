package com.example.smartcutapp.domain.usecase

import com.example.smartcutapp.domain.model.User
import com.example.smartcutapp.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): User {
        return repository.login(email, password)
    }
}