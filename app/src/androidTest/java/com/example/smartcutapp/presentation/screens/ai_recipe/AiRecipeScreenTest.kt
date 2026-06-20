package com.example.smartcutapp.presentation.screens.ai_recipe

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
class AiRecipeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        PreferencesManager.init(composeTestRule.activity)
    }

    private fun setScreen() {
        composeTestRule.setContent {
            SmartCutAppTheme {
                AiRecipeScreen(navController = rememberNavController())
            }
        }
    }

    @Test
    fun shows_title() {
        setScreen()
        composeTestRule.onNodeWithText("AI Помощник по рецептам").assertIsDisplayed()
    }

    @Test
    fun shows_empty_state_hint() {
        setScreen()
        composeTestRule.onNodeWithText("Спросите AI о рецепте").assertIsDisplayed()
    }

    @Test
    fun shows_input_placeholder() {
        setScreen()
        composeTestRule.onNodeWithText("Спросите о рецепте...").assertIsDisplayed()
    }

    @Test
    fun send_button_disabled_when_input_empty() {
        setScreen()
        composeTestRule.onNodeWithContentDescription("Отправить").assertIsNotEnabled()
    }

    @Test
    fun send_button_enabled_after_typing() {
        setScreen()
        composeTestRule.onNodeWithText("Спросите о рецепте...").performTextInput("Рецепт борща")
        composeTestRule.onNodeWithContentDescription("Отправить").assertIsEnabled()
    }

    @Test
    fun typed_text_is_shown_in_input() {
        setScreen()
        composeTestRule.onNodeWithText("Спросите о рецепте...").performTextInput("борщ")
        composeTestRule.onNodeWithText("борщ").assertIsDisplayed()
    }

    @Test
    fun send_button_disabled_after_clearing_input() {
        setScreen()
        composeTestRule.onNodeWithText("Спросите о рецепте...").performTextInput("борщ")
        composeTestRule.onNodeWithContentDescription("Отправить").assertIsEnabled()
        composeTestRule.onNodeWithText("борщ").performTextClearance()
        composeTestRule.onNodeWithContentDescription("Отправить").assertIsNotEnabled()
    }

    @Test
    fun shows_example_prompt_hint() {
        setScreen()
        composeTestRule.onNodeWithText(
            "Например: «Рецепт греческого салата» или\n«Что приготовить из картофеля и курицы?»",
            substring = true
        ).assertIsDisplayed()
    }

    @Test
    fun no_delete_button_when_chat_is_empty() {
        setScreen()
        composeTestRule.onNodeWithContentDescription("Очистить чат").assertDoesNotExist()
    }
}
