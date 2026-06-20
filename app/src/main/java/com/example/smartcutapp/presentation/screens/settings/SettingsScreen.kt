package com.example.smartcutapp.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.data.local.ThemeMode
import com.example.smartcutapp.presentation.navigation.Screen
import com.example.smartcutapp.ui.theme.LocalDarkTheme

@Composable
fun SettingsScreen(navController: NavController) {
    val darkTheme = LocalDarkTheme.current
    val viewModel: SettingsViewModel = viewModel()

    val isConnected by viewModel.isConnected.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val brokerUrl by viewModel.brokerUrl.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()
    val esp32Online by viewModel.esp32Online.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val mistralApiKey by viewModel.mistralApiKey.collectAsState()
    val esp32CamUrl by viewModel.esp32CamUrl.collectAsState()
    val ollamaUrl by viewModel.ollamaUrl.collectAsState()
    val ollamaModel by viewModel.ollamaModel.collectAsState()

    var showMistralKey by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    text = "Настройки",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onPrimary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- ТЕМА ---
                Text(
                    text = "ВНЕШНИЙ ВИД",
                    style = MaterialTheme.typography.labelSmall,
                    color = SmartCutColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Тема приложения",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeModeButton(
                                label = "Авто",
                                selected = themeMode == ThemeMode.SYSTEM,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                            )
                            ThemeModeButton(
                                label = "Светлая",
                                selected = themeMode == ThemeMode.LIGHT,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                            )
                            ThemeModeButton(
                                label = "Тёмная",
                                selected = themeMode == ThemeMode.DARK,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                            )
                        }
                    }
                }

                // --- AI КЛЮЧ ---
                Text(
                    text = "AI ПОМОЩНИК",
                    style = MaterialTheme.typography.labelSmall,
                    color = SmartCutColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Mistral API ключ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "platform.mistral.ai → API keys — для чата с рецептами",
                            style = MaterialTheme.typography.bodySmall,
                            color = SmartCutColors.TextSecondary
                        )
                        OutlinedTextField(
                            value = mistralApiKey,
                            onValueChange = { viewModel.setMistralApiKey(it) },
                            label = { Text("...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (showMistralKey) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                TextButton(onClick = { showMistralKey = !showMistralKey }) {
                                    Text(
                                        text = if (showMistralKey) "Скрыть" else "Показать",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline
                        )

                        Text(
                            text = "Ollama — адрес сервера",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "IP ПК в локальной сети с запущенным Ollama — для распознавания продуктов",
                            style = MaterialTheme.typography.bodySmall,
                            color = SmartCutColors.TextSecondary
                        )
                        OutlinedTextField(
                            value = ollamaUrl,
                            onValueChange = { viewModel.setOllamaUrl(it) },
                            label = { Text("http://192.168.1.100:11434") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Text(
                            text = "Ollama — модель",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "llava (рекомендуется) или moondream для слабых ПК",
                            style = MaterialTheme.typography.bodySmall,
                            color = SmartCutColors.TextSecondary
                        )
                        OutlinedTextField(
                            value = ollamaModel,
                            onValueChange = { viewModel.setOllamaModel(it) },
                            label = { Text("llava") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // --- ESP32 КАМЕРА ---
                Text(
                    text = "ESP32 КАМЕРА",
                    style = MaterialTheme.typography.labelSmall,
                    color = SmartCutColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Адрес ESP32-CAM",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "IP-адрес модуля в локальной сети (без /capture)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SmartCutColors.TextSecondary
                        )
                        OutlinedTextField(
                            value = esp32CamUrl,
                            onValueChange = { viewModel.setEsp32CamUrl(it) },
                            label = { Text("http://192.168.4.1") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // --- MQTT ---
                Text(
                    text = "ESP32 / MQTT",
                    style = MaterialTheme.typography.labelSmall,
                    color = SmartCutColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusRow(
                            label = "Брокер MQTT",
                            connected = isConnected,
                            onText = "Подключено",
                            offText = "Отключено"
                        )

                        if (isConnected) {
                            StatusRow(
                                label = "ESP32",
                                connected = esp32Online,
                                onText = "Онлайн",
                                offText = "Не найден"
                            )
                        }

                        OutlinedTextField(
                            value = brokerUrl,
                            onValueChange = { viewModel.setBrokerUrl(it) },
                            label = { Text("Адрес брокера MQTT") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        if (deviceStatus.isNotEmpty()) {
                            Text(
                                text = "Последнее сообщение: $deviceStatus",
                                style = MaterialTheme.typography.bodySmall,
                                color = SmartCutColors.TextSecondary
                            )
                        }

                        if (error != null) {
                            Text(
                                text = error ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleConnection() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isConnecting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = if (isConnected) "Отключиться" else "Подключиться",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                // --- ПРИЛОЖЕНИЕ ---
                Text(
                    text = "ПРИЛОЖЕНИЕ",
                    style = MaterialTheme.typography.labelSmall,
                    color = SmartCutColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Язык",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Русский",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SmartCutColors.TextSecondary
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Версия",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "1.0.0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SmartCutColors.TextSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (isLoggedIn) {
                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "Выйти из аккаунта",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                } else {
                    Button(
                        onClick = { navController.navigate(Screen.Register.route) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Зарегистрироваться",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ThemeModeButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StatusRow(
    label: String,
    connected: Boolean,
    onText: String,
    offText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (connected) Color(0xFF4CAF50) else Color(0xFFE53935),
                        shape = RoundedCornerShape(5.dp)
                    )
            )
            Text(
                text = if (connected) onText else offText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (connected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
        }
    }
}
