package com.example.smartcutapp.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object ThemeManager {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        PreferencesManager.themeMode = mode.ordinal
    }

    fun init() {
        _themeMode.value = ThemeMode.entries.getOrElse(PreferencesManager.themeMode) { ThemeMode.SYSTEM }
    }
}
