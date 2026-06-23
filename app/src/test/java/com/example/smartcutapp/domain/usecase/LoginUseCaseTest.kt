package com.example.smartcutapp.domain.usecase

import com.example.smartcutapp.domain.model.User
import com.example.smartcutapp.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LoginUseCaseTest {

    private val fakeUser = User(id = 1, name = "Тестовый пользователь", token = "tok123")

    private val successRepo = object : AuthRepository {
        override suspend fun register(name: String, email: String, password: String) = fakeUser
        override suspend fun login(email: String, password: String) = fakeUser
        override suspend fun logout() {}
        override suspend fun getToken(): String? = "tok123"
    }

    private val failRepo = object : AuthRepository {
        override suspend fun register(name: String, email: String, password: String) = error("not used")
        override suspend fun login(email: String, password: String) =
            throw RuntimeException("Неверный пароль")
        override suspend fun logout() {}
        override suspend fun getToken(): String? = null
    }

    @Test
    fun `invoke returns user from repository on success`() = runTest {
        val useCase = LoginUseCase(successRepo)
        val result = useCase("test@test.com", "password")
        assertEquals(fakeUser, result)
    }

    @Test
    fun `invoke passes email and password to repository`() = runTest {
        var capturedEmail = ""
        var capturedPassword = ""
        val capturingRepo = object : AuthRepository {
            override suspend fun login(email: String, password: String): User {
                capturedEmail = email
                capturedPassword = password
                return fakeUser
            }
            override suspend fun register(name: String, email: String, password: String) = fakeUser
            override suspend fun logout() {}
            override suspend fun getToken(): String? = null
        }
        LoginUseCase(capturingRepo)("user@mail.com", "secret")
        assertEquals("user@mail.com", capturedEmail)
        assertEquals("secret", capturedPassword)
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        val useCase = LoginUseCase(failRepo)
        val ex = runCatching { useCase("x@y.com", "wrong") }.exceptionOrNull()
        assertNotNull(ex)
        assertEquals("Неверный пароль", ex?.message)
    }
}
