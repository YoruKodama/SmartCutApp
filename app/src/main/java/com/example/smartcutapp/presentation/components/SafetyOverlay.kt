package com.example.smartcutapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartcutapp.data.mqtt.MqttManager

private val WarnAmber = Color(0xFFFFA000)

/**
 * Поверх любого экрана: предупреждение о потере связи.
 * Пока связь потеряна, приложение новых команд не шлёт, а цикл на устройстве доделывается сам.
 */
@Composable
fun SafetyOverlay(modifier: Modifier = Modifier) {
    val linkLost by MqttManager.linkLost.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (linkLost) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(WarnAmber)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.WifiOff, contentDescription = null, tint = Color.Black)
                Text(
                    text = "Связь с устройством потеряна. Идущую нарезку устройство доделает само, " +
                        "новые команды не отправляются. Переподключаюсь…",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
