package com.example.smartcutapp.presentation.screens.journal

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.presentation.components.AppHeader
import com.example.smartcutapp.presentation.components.NutritionRow
import com.example.smartcutapp.presentation.components.SectionCard
import com.example.smartcutapp.presentation.components.rememberPhotoPicker
import com.example.smartcutapp.presentation.screens.product_scan.ProductScanViewModel

@Composable
fun JournalScreen(navController: NavController) {
    val viewModel: JournalViewModel = viewModel()
    val scan: ProductScanViewModel = viewModel()
    val plan by viewModel.plan.collectAsState()

    var meals by remember { mutableStateOf("3") }
    var preferences by remember { mutableStateOf("") }
    var planTarget by remember { mutableStateOf(viewModel.defaultKcal.toString()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AppHeader(title = "Питание")

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { ScanCard(scan) }

                item {
                    SectionCard {
                        Text("Рацион от нейросети", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Составит рацион на нужное число калорий",
                            style = MaterialTheme.typography.bodySmall,
                            color = SmartCutColors.TextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = planTarget,
                                onValueChange = { planTarget = it.filter(Char::isDigit).take(5) },
                                label = { Text("Ккал") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = meals,
                                onValueChange = { meals = it.filter(Char::isDigit).take(1) },
                                label = { Text("Приёмов") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = preferences,
                            onValueChange = { preferences = it },
                            label = { Text("Пожелания (необязательно)") },
                            placeholder = { Text("например: без мяса, больше белка") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.buildPlan(
                                    targetKcal = planTarget.toIntOrNull() ?: viewModel.defaultKcal,
                                    meals = (meals.toIntOrNull() ?: 3).coerceIn(1, 6),
                                    preferences = preferences
                                )
                            },
                            enabled = !plan.isLoading,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (plan.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Составить рацион")
                        }
                        plan.error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                        }
                        plan.text?.let {
                            Spacer(Modifier.height(10.dp))
                            Text(it, color = MaterialTheme.colorScheme.onSurface)
                            TextButton(onClick = viewModel::clearPlan) { Text("Скрыть") }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

/** Фото продукта с камеры → название и КБЖУ (на 100 г и на указанную массу). */
@Composable
private fun ScanCard(viewModel: ProductScanViewModel) {
    val image by viewModel.capturedImage.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val nameRu by viewModel.nameRu.collectAsState()
    val preset by viewModel.detectedPreset.collectAsState()
    val nutrition by viewModel.nutrition.collectAsState()
    val nutritionLoading by viewModel.nutritionLoading.collectAsState()
    val nutritionError by viewModel.nutritionError.collectAsState()
    val grams by viewModel.grams.collectAsState()
    val error by viewModel.error.collectAsState()
    val isBusy = isAnalyzing
    val context = LocalContext.current
    val photoPicker = rememberPhotoPicker { uri -> viewModel.analyze(context, uri) }

    SectionCard {
        Text("КБЖУ продукта по фото", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Сфотографируйте продукт или выберите фото",
            style = MaterialTheme.typography.bodySmall,
            color = SmartCutColors.TextSecondary
        )
        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val bytes = image
            when {
                isBusy -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Распознаю продукт…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SmartCutColors.TextSecondary
                    )
                }
                bytes != null -> {
                    val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Снимок продукта",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                else -> Text("Фото пока нет", color = SmartCutColors.TextSecondary)
            }
        }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { photoPicker.camera() },
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Сфотографировать продукт", style = MaterialTheme.typography.titleMedium) }
        OutlinedButton(
            onClick = { photoPicker.gallery() },
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Выбрать из галереи") }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        if (preset != null) {
            val name = nameRu ?: preset!!.productNameRu
            Spacer(Modifier.height(12.dp))
            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            when {
                nutritionLoading -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Считаю КБЖУ…")
                }
                nutrition != null -> {
                    NutritionRow("На 100 г", nutrition!!.per100g, highlight = true)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Масса", color = SmartCutColors.TextSecondary)
                        OutlinedButton(onClick = { viewModel.setGrams(grams - 10) }) { Text("−") }
                        Text(
                            "$grams г",
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
                        "Оценка нейросети по сырому продукту, приблизительно",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartCutColors.TextSecondary
                    )
                }
                else -> {
                    Text(nutritionError ?: "КБЖУ не рассчитано", color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { viewModel.retryNutrition() }) { Text("Повторить расчёт") }
                }
            }
        }
    }
}
