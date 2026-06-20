package com.example.smartcutapp.presentation.screens.login

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.ui.theme.SmartCutAppTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        PreferencesManager.init(composeTestRule.activity)
    }

    private fun setScreen() {
        composeTestRule.setContent {
            SmartCutAppTheme {
                LoginScreen(navController = rememberNavController())
            }
        }
    }

    @Test
    fun shows_title() {
        setScreen()
        composeTestRule.onNodeWithText("Вход").assertIsDisplayed()
    }

    @Test
    fun shows_email_field() {
        setScreen()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun shows_password_field() {
        setScreen()
        composeTestRule.onNodeWithText("Пароль").assertIsDisplayed()
    }

    @Test
    fun login_button_disabled_when_fields_empty() {
        setScreen()
        composeTestRule.onNodeWithText("Войти").assertIsNotEnabled()
    }

    @Test
    fun login_button_disabled_when_only_email_filled() {
        setScreen()
        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Войти").assertIsNotEnabled()
    }

    @Test
    fun login_button_disabled_when_only_password_filled() {
        setScreen()
        composeTestRule.onNodeWithText("Пароль").performTextInput("secret123")
        composeTestRule.onNodeWithText("Войти").assertIsNotEnabled()
    }

    @Test
    fun login_button_enabled_when_both_fields_filled() {
        setScreen()
        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Пароль").performTextInput("secret123")
        composeTestRule.onNodeWithText("Войти").assertIsEnabled()
    }

    @Test
    fun shows_register_link() {
        setScreen()
        composeTestRule.onNodeWithText("Нет аккаунта? Зарегистрироваться").assertIsDisplayed()
    }

    @Test
    fun typing_in_email_field_shows_text() {
        setScreen()
        composeTestRule.onNodeWithText("Email").performTextInput("hello@mail.ru")
        composeTestRule.onNodeWithText("hello@mail.ru").assertIsDisplayed()
    }
}
