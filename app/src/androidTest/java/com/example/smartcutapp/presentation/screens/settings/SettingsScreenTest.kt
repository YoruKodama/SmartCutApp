package com.example.smartcutapp.presentation.screens.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.data.local.ThemeManager
import com.example.smartcutapp.ui.theme.SmartCutAppTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        PreferencesManager.init(composeTestRule.activity)
        ThemeManager.init()
    }

    private fun setScreen() {
        composeTestRule.setContent {
            SmartCutAppTheme {
                SettingsScreen(navController = rememberNavController())
            }
        }
    }

    @Test
    fun shows_title() {
        setScreen()
        composeTestRule.onNodeWithText("Настройки").assertIsDisplayed()
    }

    @Test
    fun shows_appearance_section() {
        setScreen()
        composeTestRule.onNodeWithText("ВНЕШНИЙ ВИД").assertIsDisplayed()
    }

    @Test
    fun shows_auto_theme_button() {
        setScreen()
        composeTestRule.onNodeWithText("Авто").assertIsDisplayed()
    }

    @Test
    fun shows_light_theme_button() {
        setScreen()
        composeTestRule.onNodeWithText("Светлая").assertIsDisplayed()
    }

    @Test
    fun shows_dark_theme_button() {
        setScreen()
        composeTestRule.onNodeWithText("Тёмная").assertIsDisplayed()
    }

    @Test
    fun shows_ai_section() {
        setScreen()
        composeTestRule.onNodeWithText("AI ПОМОЩНИК").assertIsDisplayed()
    }

    @Test
    fun shows_mistral_api_key_label() {
        setScreen()
        composeTestRule.onNodeWithText("Mistral API ключ").assertIsDisplayed()
    }

    @Test
    fun shows_ollama_url_label() {
        setScreen()
        composeTestRule.onNodeWithText("Ollama — адрес сервера").assertIsDisplayed()
    }

    @Test
    fun shows_ollama_model_label() {
        setScreen()
        composeTestRule.onNodeWithText("Ollama — модель").assertIsDisplayed()
    }

    @Test
    fun shows_mqtt_section() {
        setScreen()
        composeTestRule.onNodeWithText("ESP32 / MQTT").assertIsDisplayed()
    }

    @Test
    fun shows_camera_section() {
        setScreen()
        composeTestRule.onNodeWithText("ESP32 КАМЕРА").assertIsDisplayed()
    }

    @Test
    fun shows_connect_button() {
        setScreen()
        composeTestRule.onNodeWithText("Подключиться").assertIsDisplayed()
    }

    @Test
    fun shows_app_section() {
        setScreen()
        composeTestRule.onNodeWithText("ПРИЛОЖЕНИЕ").assertIsDisplayed()
    }

    @Test
    fun clicking_dark_theme_button_does_not_crash() {
        setScreen()
        composeTestRule.onNodeWithText("Тёмная").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Тёмная").assertIsDisplayed()
    }

    @Test
    fun clicking_light_theme_button_does_not_crash() {
        setScreen()
        composeTestRule.onNodeWithText("Светлая").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Светлая").assertIsDisplayed()
    }
}
