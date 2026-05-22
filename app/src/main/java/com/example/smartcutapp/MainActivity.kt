package com.example.smartcutapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.rememberNavController
import com.example.smartcutapp.presentation.components.BottomBar
import com.example.smartcutapp.presentation.navigation.NavGraph
import com.example.smartcutapp.ui.theme.SmartCutAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCutAppTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomBar(navController) }
                ) { padding ->
                    NavGraph(navController, padding)
                }
            }
        }
    }
}