package com.example.smartcutapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.smartcutapp.presentation.screens.main.MainScreen
import com.example.smartcutapp.ui.theme.SmartCutAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCutAppTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
    }
}