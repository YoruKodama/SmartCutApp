package com.example.smartcutapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartcutapp.data.local.ThemeManager
import com.example.smartcutapp.data.local.ThemeMode
import com.example.smartcutapp.presentation.components.BottomBar
import com.example.smartcutapp.presentation.components.SafetyOverlay
import com.example.smartcutapp.presentation.navigation.NavGraph
import com.example.smartcutapp.presentation.navigation.Screen
import com.example.smartcutapp.ui.theme.SmartCutAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init()
        enableEdgeToEdge()
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }
            SmartCutAppTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val route = navController.currentBackStackEntryAsState().value?.destination?.route
                val isAuthScreen = route == Screen.Login.route || route == Screen.Register.route
                Scaffold(
                    bottomBar = { BottomBar(navController) }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        NavGraph(navController, PaddingValues())
                        if (!isAuthScreen) SafetyOverlay()
                    }
                }
            }
        }
    }
}
