package com.example.smartcutapp.presentation.screens.weighing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcutapp.R
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.presentation.navigation.Screen
import com.example.smartcutapp.ui.theme.LocalDarkTheme
import kotlin.math.abs

@Composable
fun WeighingScreen(navController: NavController) {
    val darkTheme = LocalDarkTheme.current
    val viewModel: WeighingViewModel = viewModel()

    val displayWeight by viewModel.displayWeight.collectAsState()
    val unit by viewModel.unit.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val esp32Online by viewModel.esp32Online.collectAsState()

    val deviceReady = isConnected && esp32Online

    val weightText = remember(displayWeight, unit) {
        when (unit) {
            WeightUnit.GRAMS -> "%.0f".format(displayWeight)
            WeightUnit.KG -> "%.3f".format(displayWeight / 1000f)
        }
    }
    val unitLabel = if (unit == WeightUnit.GRAMS) "г" else "кг"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Camera.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Распознать продукт",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (darkTheme) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.primary
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Взвешивание",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (deviceReady) Color(0xFF4CAF50)
                                else if (isConnected) Color(0xFFFFC107)
                                else Color(0xFFE53935)
                            )
                    )
                    Text(
                        text = when {
                            deviceReady -> "ESP32 онлайн"
                            isConnected -> "Ожидание ESP32"
                            else -> "Нет соединения"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (darkTheme) MaterialTheme.colorScheme.onSurface.copy(0.7f)
                        else MaterialTheme.colorScheme.onPrimary.copy(0.85f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Scales icon
                Icon(
                    painter = painterResource(id = R.drawable.scales),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                // Weight display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!deviceReady) {
                            Text(
                                text = "—",
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = SmartCutColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = weightText,
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = unitLabel,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = SmartCutColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Unit toggle
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UnitButton(
                            label = "г",
                            selected = unit == WeightUnit.GRAMS,
                            onClick = { viewModel.setUnit(WeightUnit.GRAMS) }
                        )
                        UnitButton(
                            label = "кг",
                            selected = unit == WeightUnit.KG,
                            onClick = { viewModel.setUnit(WeightUnit.KG) }
                        )
                    }
                }

                // Tare buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.tare() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = deviceReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Тарировать",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.resetTare() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Сброс")
                    }
                }

                // Status card
                if (!deviceReady) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Для взвешивания:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SmartCutColors.TextSecondary
                            )
                            Text(
                                text = "1. Подключитесь к MQTT брокеру в Настройках\n" +
                                       "2. Убедитесь, что ESP32 включён и публикует данные в топик smartcut/weight",
                                style = MaterialTheme.typography.bodySmall,
                                color = SmartCutColors.TextSecondary
                            )
                        }
                    }
                }

                // Camera hint
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Распознавание продуктов",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Нажмите кнопку камеры, чтобы идентифицировать продукт",
                                style = MaterialTheme.typography.bodySmall,
                                color = SmartCutColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
    }
}
