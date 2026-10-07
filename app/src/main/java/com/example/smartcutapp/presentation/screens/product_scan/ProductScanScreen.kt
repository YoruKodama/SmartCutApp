package com.example.smartcutapp.presentation.screens.product_scan

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.presentation.components.NutritionRow
import com.example.smartcutapp.presentation.components.SectionCard
import com.example.smartcutapp.presentation.components.rememberPhotoPicker
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScanScreen(navController: NavController) {
    val darkTheme = isSystemInDarkTheme()
    val viewModel: ProductScanViewModel = viewModel()

    val capturedImage by viewModel.capturedImage.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val nameRu by viewModel.nameRu.collectAsState()
    val detectedPreset by viewModel.detectedPreset.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val nutrition by viewModel.nutrition.collectAsState()
    val nutritionLoading by viewModel.nutritionLoading.collectAsState()
    val nutritionError by viewModel.nutritionError.collectAsState()
    val grams by viewModel.grams.collectAsState()
    val error by viewModel.error.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isBusy = isAnalyzing
    val context = LocalContext.current
    val photoPicker = rememberPhotoPicker { uri -> viewModel.analyze(context, uri) }

    LaunchedEffect(sendResult) {
        if (sendResult != null) {
            delay(3000)
            viewModel.clearSendResult()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Header
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Распознавание продукта",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (darkTheme) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "AI подберёт режим нарезки",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (darkTheme) SmartCutColors.TextSecondary
                        else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                    )
                }
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
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Image preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        capturedImage != null && !isBusy -> {
                            val bitmap = remember(capturedImage) {
                                BitmapFactory.decodeByteArray(capturedImage, 0, capturedImage!!.size)
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Снимок продукта",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        isBusy -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Распознаю продукт...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SmartCutColors.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "📷",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Text(
                                    text = "Сфотографируйте продукт\nили выберите фото из галереи",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SmartCutColors.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Photo buttons
                Button(
                    onClick = { photoPicker.camera() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (isAnalyzing) "AI анализирует..." else "Сфотографировать",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (!isBusy) MaterialTheme.colorScheme.onPrimary
                        else SmartCutColors.TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                OutlinedButton(
                    onClick = { photoPicker.gallery() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isBusy
                ) {
                    Text("Выбрать из галереи", modifier = Modifier.padding(vertical = 4.dp))
                }

                // Error
                if (error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = error ?: "",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Result section
                if (detectedPreset != null) {

                    // Detected product card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "РАСПОЗНАННЫЙ ПРОДУКТ",
                                style = MaterialTheme.typography.labelSmall,
                                color = SmartCutColors.TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = detectedPreset!!.emoji,
                                    style = MaterialTheme.typography.displaySmall
                                )
                                Column {
                                    Text(
                                        text = nameRu ?: detectedPreset!!.productNameRu,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // КБЖУ распознанного продукта
                    SectionCard {
                        Text(
                            text = "КБЖУ ПРОДУКТА",
                            style = MaterialTheme.typography.labelSmall,
                            color = SmartCutColors.TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        when {
                            nutritionLoading -> Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text("Считаю КБЖУ…", style = MaterialTheme.typography.bodyMedium)
                            }
                            nutrition != null -> {
                                NutritionRow("На 100 г", nutrition!!.per100g, highlight = true)
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Масса", style = MaterialTheme.typography.bodyMedium, color = SmartCutColors.TextSecondary)
                                    OutlinedButton(onClick = { viewModel.setGrams(grams - 10) }) { Text("−") }
                                    Text(
                                        text = "$grams г",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.width(64.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    OutlinedButton(onClick = { viewModel.setGrams(grams + 10) }) { Text("+") }
                                }
                                Spacer(Modifier.height(8.dp))
                                NutritionRow("На $grams г", nutrition!!.forGrams(grams.toDouble()))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Оценка нейросети по сырому продукту, приблизительно",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SmartCutColors.TextSecondary
                                )
                            }
                            else -> {
                                Text(
                                    text = nutritionError ?: "КБЖУ не рассчитано",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(onClick = { viewModel.retryNutrition() }) { Text("Повторить расчёт") }
                            }
                        }
                    }

                    // Cutting preset card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "РЕКОМЕНДУЕМЫЙ РЕЖИМ НАРЕЗКИ",
                                style = MaterialTheme.typography.labelSmall,
                                color = SmartCutColors.TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = detectedPreset!!.cut.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                            Text(
                                text = detectedPreset!!.cut.attachment,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SmartCutColors.TextSecondary
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                            // Speed
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Скорость",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SmartCutColors.TextSecondary
                                )
                                Text(
                                    text = "${(speed * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Медленно", style = MaterialTheme.typography.bodySmall, color = SmartCutColors.TextSecondary)
                                Text("Быстро", style = MaterialTheme.typography.bodySmall, color = SmartCutColors.TextSecondary)
                            }
                            val sliderInteraction = remember { MutableInteractionSource() }
                            Slider(
                                value = speed,
                                onValueChange = { viewModel.setSpeed(it) },
                                modifier = Modifier.fillMaxWidth(),
                                interactionSource = sliderInteraction,
                                thumb = {
                                    SliderDefaults.Thumb(
                                        interactionSource = sliderInteraction,
                                        thumbSize = DpSize(24.dp, 24.dp)
                                    )
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }

                    // Send result message
                    if (sendResult != null) {
                        Text(
                            text = sendResult ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (sendResult?.startsWith("Ошибка") == true)
                                MaterialTheme.colorScheme.error
                            else Color(0xFF4CAF50),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (!isConnected) {
                        Text(
                            text = "Устройство не подключено — подключитесь в разделе Настройки",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // Apply button
                    Button(
                        onClick = { viewModel.applyPreset() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isConnected && !isSending,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = "Применить режим к SlicerBot",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isConnected) Color.White else SmartCutColors.TextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Rescan
                    OutlinedButton(
                        onClick = { viewModel.clearAll() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Распознать другой продукт",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
