package com.example.smartcutapp.presentation.screens.blade

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcutapp.R
import com.example.smartcutapp.app.ui.theme.SmartCutColors

@Composable
fun BladeCubeScreen(navController: NavController, ingredient: String = "") {
    val darkTheme = isSystemInDarkTheme()
    val viewModel: BladeSettingsViewModel = viewModel()

    val width by viewModel.width.collectAsState()
    val height by viewModel.height.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    LaunchedEffect(sendResult) {
        if (sendResult != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearResult()
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
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (darkTheme) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.primary
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = if (darkTheme) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = if (ingredient.isNotEmpty()) "Кубиками: $ingredient" else "Нарезка кубиками",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(10.dp)
                        .background(
                            color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFE53935),
                            shape = RoundedCornerShape(5.dp)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.salad_svgrepo_com__1_),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Text(
                    text = "Размер куба",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SizeInput(
                        label = "Ширина",
                        value = width,
                        onMinus = { if (width > 1) viewModel.setWidth(width - 1) },
                        onPlus = { if (width < 50) viewModel.setWidth(width + 1) },
                        modifier = Modifier.weight(1f)
                    )
                    SizeInput(
                        label = "Высота",
                        value = height,
                        onMinus = { if (height > 1) viewModel.setHeight(height - 1) },
                        onPlus = { if (height < 50) viewModel.setHeight(height + 1) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Скорость нарезки",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Медленно", style = MaterialTheme.typography.bodyMedium, color = SmartCutColors.TextSecondary)
                    Text(text = "Быстро", style = MaterialTheme.typography.bodyMedium, color = SmartCutColors.TextSecondary)
                }

                Slider(
                    value = speed,
                    onValueChange = { viewModel.setSpeed(it) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                if (sendResult != null) {
                    Text(
                        text = sendResult ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (sendResult?.startsWith("Ошибка") == true)
                            MaterialTheme.colorScheme.error
                        else Color(0xFF4CAF50)
                    )
                }

                if (!isConnected) {
                    Text(
                        text = "ESP32 не подключён. Настройте MQTT в разделе Настройки.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.sendCommand() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isConnected && !isSending,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Применить",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SizeInput(
    label: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = SmartCutColors.TextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onMinus) {
                    Text(text = "−", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "$value мм",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onPlus) {
                    Text(text = "+", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
