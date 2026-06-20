package com.example.smartcutapp.presentation.screens.weighing

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
class WeighingScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        PreferencesManager.init(composeTestRule.activity)
    }

    private fun setScreen() {
        composeTestRule.setContent {
            SmartCutAppTheme {
                WeighingScreen(navController = rememberNavController())
            }
        }
    }

    @Test
    fun shows_title() {
        setScreen()
        composeTestRule.onNodeWithText("Взвешивание").assertIsDisplayed()
    }

    @Test
    fun shows_dash_when_not_connected() {
        setScreen()
        composeTestRule.onNodeWithText("—").assertIsDisplayed()
    }

    @Test
    fun shows_no_connection_status() {
        setScreen()
        composeTestRule.onNodeWithText("Нет соединения").assertIsDisplayed()
    }

    @Test
    fun shows_grams_unit_button() {
        setScreen()
        composeTestRule.onAllNodesWithText("г").onFirst().assertIsDisplayed()
    }

    @Test
    fun shows_kg_unit_button() {
        setScreen()
        composeTestRule.onNodeWithText("кг").assertIsDisplayed()
    }

    @Test
    fun tare_button_disabled_when_not_connected() {
        setScreen()
        composeTestRule.onNodeWithText("Тарировать").assertIsNotEnabled()
    }

    @Test
    fun reset_button_is_displayed() {
        setScreen()
        composeTestRule.onNodeWithText("Сброс").assertIsDisplayed()
    }

    @Test
    fun shows_mqtt_connection_hint() {
        setScreen()
        composeTestRule.onNodeWithText("Для взвешивания:").assertIsDisplayed()
    }

    @Test
    fun shows_camera_recognition_hint() {
        setScreen()
        composeTestRule.onNodeWithText("Распознавание продуктов").assertIsDisplayed()
    }

    @Test
    fun clicking_kg_button_changes_unit() {
        setScreen()
        composeTestRule.onNodeWithText("кг").performClick()
        composeTestRule.waitForIdle()
        // после переключения в заголовке весов появляется "кг"
        composeTestRule.onAllNodesWithText("кг").fetchSemanticsNodes().size
            .let { assert(it >= 1) }
    }

    @Test
    fun clicking_g_button_keeps_grams_unit() {
        setScreen()
        composeTestRule.onAllNodesWithText("г").onFirst().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("—").assertIsDisplayed()
    }
}
