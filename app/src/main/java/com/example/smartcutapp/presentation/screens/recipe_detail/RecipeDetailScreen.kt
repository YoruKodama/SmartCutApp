package com.example.smartcutapp.presentation.screens.recipe_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartcutapp.R
import com.example.smartcutapp.data.remote.api.ApiClient
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.usecase.RecipePlanner
import com.example.smartcutapp.presentation.components.AppHeader
import com.example.smartcutapp.presentation.components.NutritionRow
import com.example.smartcutapp.presentation.components.SectionCard
import com.example.smartcutapp.presentation.navigation.Screen

@Composable
fun RecipeDetailScreen(navController: NavController, recipeId: Int) {
    val viewModel: RecipeDetailViewModel = viewModel()

    val recipe by viewModel.recipe.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val deleted by viewModel.deleted.collectAsState()
    val servings by viewModel.servings.collectAsState()
    val products by viewModel.products.collectAsState()
    val isCalculating by viewModel.isCalculating.collectAsState()
    val nutritionError by viewModel.nutritionError.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Перезагружаем при возврате на экран (например, после правки рецепта)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, recipeId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadRecipe(recipeId)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(deleted) {
        if (deleted) navController.popBackStack()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить рецепт?") },
            text = { Text("Это действие нельзя отменить") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecipe(recipeId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
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
            AppHeader(
                title = recipe?.name ?: "",
                onBack = { navController.popBackStack() }
            ) {
                IconButton(onClick = { navController.navigate(Screen.CreateRecipe.createRoute(recipeId)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Править")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                }
            }

            when {
                recipe == null && isLoading -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                recipe == null && error != null -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
                recipe != null -> {
                    val r = recipe!!
                    RecipeContent(
                        r = r,
                        servings = servings,
                        onServings = viewModel::setServings,
                        nutritionTotal = viewModel.totalNutrition(r, servings, products),
                        hasNutrition = products.isNotEmpty(),
                        isCalculating = isCalculating,
                        nutritionError = nutritionError,
                        products = products,
                        onCalculate = viewModel::calculateNutrition,
                        onStart = { navController.navigate(Screen.Cooking.createRoute(r.id, servings)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeContent(
    r: Recipe,
    servings: Int,
    onServings: (Int) -> Unit,
    nutritionTotal: com.example.smartcutapp.domain.model.Nutrition,
    hasNutrition: Boolean,
    isCalculating: Boolean,
    nutritionError: String?,
    products: Map<Int, com.example.smartcutapp.domain.model.ProductNutrition>,
    onCalculate: () -> Unit,
    onStart: () -> Unit
) {
    val plan = remember(r) { RecipePlanner.attachmentPlan(r) }
    val available = remember { PreferencesManager.availableAttachments }
    val missing = plan.map { it.cutType }.filter { it !in available }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!r.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ApiClient.resolveImageUrl(r.imageUrl),
                contentDescription = r.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.salad_svgrepo_com__1_),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = r.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            val meta = listOfNotNull(r.cookingTime.ifEmpty { null }, r.tags.joinToString(" · ").ifEmpty { null })
            if (meta.isNotEmpty()) {
                Text(
                    text = meta.joinToString("  •  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmartCutColors.TextSecondary
                )
            }
        }

        // Порции
        SectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Порции", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Массы пересчитываются автоматически",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartCutColors.TextSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { onServings(servings - 1) }, modifier = Modifier.size(44.dp)) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "$servings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    FilledTonalIconButton(onClick = { onServings(servings + 1) }, modifier = Modifier.size(44.dp)) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        // Насадки по порядку
        if (plan.isNotEmpty()) {
            SectionCard {
                Text("Насадки по порядку", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Ингредиенты с одной насадкой идут подряд — её не придётся менять лишний раз",
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartCutColors.TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                plan.forEachIndexed { i, group ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("${i + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(
                                group.cutType.attachment,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                group.ingredients.joinToString(", ") { it.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = SmartCutColors.TextSecondary
                            )
                        }
                    }
                }
                if (missing.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "У вас нет: ${missing.joinToString { it.label }} (настройте в «Устройство и насадки»)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Ингредиенты
        Text(
            text = "Ингредиенты",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        r.ingredients.forEach { ing ->
            IngredientRow(
                recipe = r,
                ingredient = ing,
                servings = servings,
                kcal = products[ing.id]?.let { info ->
                    val grams = RecipePlanner.scaledGrams(r, ing, servings)
                        ?: info.estimatedGrams?.let { RecipePlanner.scaleGrams(it, r.servings, servings) }
                    grams?.let { info.forGrams(it.toDouble()).kcal.toInt() }
                }
            )
        }

        // Ккал и БЖУ
        SectionCard {
            Text("Калорийность", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            if (hasNutrition) {
                NutritionRow("Всего на $servings порц.", nutritionTotal, highlight = true)
                Spacer(Modifier.height(8.dp))
                NutritionRow("На одну порцию", nutritionTotal / servings.toDouble())
                Spacer(Modifier.height(4.dp))
                Text(
                    "Оценка нейросети по сырым продуктам, приблизительно",
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartCutColors.TextSecondary
                )
            }
            if (nutritionError != null) {
                Text(nutritionError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onCalculate,
                enabled = !isCalculating,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isCalculating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Считаю…")
                } else {
                    Text(if (hasNutrition) "Пересчитать нейросетью" else "Посчитать ккал и БЖУ (нейросеть)")
                }
            }
        }

        // Шаги
        if (r.steps.isNotEmpty()) {
            Text(
                text = "Приготовление",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            r.steps.forEachIndexed { i, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${i + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(step, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        Button(
            onClick = onStart,
            enabled = plan.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (plan.isNotEmpty()) "Начать готовку" else "Нет ингредиентов для нарезки",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun IngredientRow(
    recipe: Recipe,
    ingredient: Ingredient,
    servings: Int,
    kcal: Int?
) {
    val grams = RecipePlanner.scaledGrams(recipe, ingredient, servings)
    val amountText = when {
        ingredient.grams != null && grams != null -> "$grams г"
        grams != null -> "$grams г"
        else -> ingredient.amount
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                val sub = buildList {
                    if (amountText.isNotEmpty()) add(amountText)
                    ingredient.cutType?.let { add(it.label) }
                    kcal?.let { add("$it ккал") }
                }
                if (sub.isNotEmpty()) {
                    Text(
                        text = sub.joinToString("  •  "),
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartCutColors.TextSecondary
                    )
                }
            }
        }
    }
}
