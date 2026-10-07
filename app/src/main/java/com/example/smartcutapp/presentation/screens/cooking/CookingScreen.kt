package com.example.smartcutapp.presentation.screens.cooking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.presentation.components.AppHeader
import com.example.smartcutapp.presentation.components.NutritionRow
import com.example.smartcutapp.presentation.components.SectionCard
import com.example.smartcutapp.presentation.components.rememberPhotoPicker
import com.example.smartcutapp.presentation.navigation.Screen

@Composable
fun CookingScreen(navController: NavController, recipeId: Int, servings: Int) {
    val viewModel: CookingViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val connected by viewModel.isConnected.collectAsState()
    val deviceOnline by viewModel.esp32Online.collectAsState()
    val linkLost by viewModel.linkLost.collectAsState()
    val autoConfirm by viewModel.autoConfirm.collectAsState()

    var confirmAuto by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId, servings) { viewModel.start(recipeId, servings) }

    // Во время готовки экран не гаснет
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    if (confirmAuto) {
        AlertDialog(
            onDismissRequest = { confirmAuto = false },
            title = { Text("Автоподтверждение старта") },
            text = {
                Text(
                    "Устройство начнёт нарезку сразу после команды из приложения, без нажатия на его экране. " +
                        "Ножи будут двигаться — убедитесь, что крышка закрыта и руки вне зоны нарезки."
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.setAutoConfirm(true); confirmAuto = false }) { Text("Включить") }
            },
            dismissButton = { OutlinedButton(onClick = { confirmAuto = false }) { Text("Отмена") } }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AppHeader(
                title = state.recipe?.name ?: "Готовка",
                onBack = { navController.popBackStack() }
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.recipe == null || state.steps.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center
                ) {
                    Text(state.error ?: "Нет данных", color = MaterialTheme.colorScheme.error)
                }
                state.finished -> FinishedContent(state, viewModel, navController)
                else -> StepContent(
                    state = state,
                    deviceReady = connected && deviceOnline && !linkLost,
                    connected = connected,
                    linkLost = linkLost,
                    autoConfirm = autoConfirm,
                    onAutoConfirm = { if (it) confirmAuto = true else viewModel.setAutoConfirm(false) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun StepContent(
    state: CookingState,
    deviceReady: Boolean,
    connected: Boolean,
    linkLost: Boolean,
    autoConfirm: Boolean,
    onAutoConfirm: (Boolean) -> Unit,
    viewModel: CookingViewModel
) {
    val step = state.steps[state.current]
    val doneCount = state.statuses.count { it == StepStatus.DONE || it == StepStatus.SKIPPED }
    val product = state.products[step.ingredient.id]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Шаг ${state.current + 1} из ${state.steps.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LinearProgressIndicator(
            progress = { doneCount.toFloat() / state.steps.size },
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )

        // Шаги можно проходить не по порядку
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(state.steps) { i, s ->
                val status = state.statuses[i]
                FilterChip(
                    selected = i == state.current,
                    onClick = { viewModel.goTo(i) },
                    label = {
                        Text(
                            (if (status == StepStatus.DONE) "✓ " else if (status == StepStatus.SKIPPED) "– " else "") +
                                s.ingredient.name
                        )
                    }
                )
            }
        }

        if (!connected || linkLost) {
            Text(
                if (linkLost) "Связь потеряна — идёт переподключение. Команды пока не отправляются."
                else "Нет связи с устройством. Подключитесь в Настройках; шаги можно отмечать вручную.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Насадка
        val bannerColor = if (step.changeAttachment) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.secondaryContainer
        val bannerText = if (step.changeAttachment) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSecondaryContainer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                if (step.changeAttachment) "Поставьте насадку" else "Насадка та же",
                style = MaterialTheme.typography.labelLarge,
                color = bannerText
            )
            Text(
                step.cutType.label,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = bannerText
            )
        }

        SectionCard {
            Text(step.ingredient.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            val target = step.grams?.let { "$it г" } ?: step.ingredient.amount
            if (target.isNotEmpty()) {
                Text(
                    "Нужно: $target",
                    style = MaterialTheme.typography.titleMedium,
                    color = SmartCutColors.TextSecondary
                )
            }
        }

        val context = LocalContext.current
        val photoPicker = rememberPhotoPicker { uri -> viewModel.checkProduct(context, uri) }
        OutlinedButton(
            onClick = { photoPicker.camera() },
            enabled = !state.isChecking,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isChecking) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
            }
            Spacer(Modifier.width(8.dp))
            Text("Проверить продукт по фото")
        }
        state.checkMessage?.let {
            Text(
                it,
                color = if (it.startsWith("✔")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = viewModel::sendCurrentStep,
            enabled = connected && !linkLost,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (state.statuses[state.current] == StepStatus.SENT) "Отправить повторно" else "Отправить на устройство",
                style = MaterialTheme.typography.titleMedium
            )
        }
        state.commandMessage?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = SmartCutColors.TextSecondary)
        }
        if (state.statuses[state.current] == StepStatus.SENT) {
            TextButton(onClick = viewModel::cancelCurrentStep) { Text("Отменить шаг на устройстве") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = viewModel::markDone,
                modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Готово", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedButton(
                onClick = viewModel::skip,
                modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Пропустить", style = MaterialTheme.typography.titleMedium) }
        }

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Автоподтверждение старта", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Режим «байпасс»: устройство стартует без нажатия на его экране",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartCutColors.TextSecondary
                    )
                }
                Switch(checked = autoConfirm, onCheckedChange = onAutoConfirm)
            }
        }

        if (state.manualIngredients.isNotEmpty()) {
            SectionCard {
                Text("Добавьте вручную (без нарезки)", fontWeight = FontWeight.SemiBold)
                state.manualIngredients.forEach {
                    Text("• ${it.name}${if (it.amount.isNotEmpty()) " — ${it.amount}" else ""}")
                }
            }
        }
    }
}

@Composable
private fun FinishedContent(state: CookingState, viewModel: CookingViewModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Готово!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Нарезано: ${state.statuses.count { it == StepStatus.DONE }} из ${state.steps.size}" +
                if (state.statuses.any { it == StepStatus.SKIPPED }) ", пропущено: ${state.statuses.count { it == StepStatus.SKIPPED }}" else "",
            color = SmartCutColors.TextSecondary
        )

        SectionCard {
            Text("Итог по блюду", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            if (state.products.isNotEmpty()) {
                NutritionRow("Всего на ${state.servings} порц.", state.total, highlight = true)
                Spacer(Modifier.height(8.dp))
                NutritionRow("На одну порцию", state.total / state.servings.toDouble())
            } else if (state.nutritionLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Калории ещё не посчитаны", color = SmartCutColors.TextSecondary)
            }
            state.nutritionError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (state.products.isEmpty() && !state.nutritionLoading) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = viewModel::calculateNutrition,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text("Посчитать ккал и БЖУ (нейросеть)") }
            }
        }

        Button(
            onClick = viewModel::saveToJournal,
            enabled = state.products.isNotEmpty() && !state.journaled,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(14.dp)
        ) { Text(if (state.journaled) "Записано в журнал ✓" else "Записать в журнал питания", style = MaterialTheme.typography.titleMedium) }

        if (state.journaled) {
            OutlinedButton(
                onClick = { navController.navigate(Screen.Journal.route) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Открыть журнал") }
        }
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("К рецепту") }
        Spacer(Modifier.height(88.dp))
    }
}
