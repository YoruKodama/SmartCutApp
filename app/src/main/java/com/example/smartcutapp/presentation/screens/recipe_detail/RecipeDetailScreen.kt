package com.example.smartcutapp.presentation.screens.recipe_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartcutapp.R
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.presentation.navigation.Screen

@Composable
fun RecipeDetailScreen(navController: NavController, recipeId: Int) {
    val darkTheme = isSystemInDarkTheme()
    val viewModel: RecipeDetailViewModel = viewModel()

    val recipe by viewModel.recipe.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val deleted by viewModel.deleted.collectAsState()

    var selectedIngredient by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    LaunchedEffect(deleted) {
        if (deleted) navController.popBackStack()
    }

    if (selectedIngredient != null) {
        AlertDialog(
            onDismissRequest = { selectedIngredient = null },
            title = { Text(selectedIngredient ?: "") },
            text = { Text("Выберите способ нарезки") },
            confirmButton = {
                Button(onClick = {
                    navController.navigate(Screen.BladeSettings.createRoute(selectedIngredient ?: ""))
                    selectedIngredient = null
                }) { Text("Кубиками") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    navController.navigate(Screen.Slices.createRoute(selectedIngredient ?: ""))
                    selectedIngredient = null
                }) { Text("Слайсами") }
            }
        )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (darkTheme) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.primary
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                    text = recipe?.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Удалить",
                        tint = if (darkTheme) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
                recipe != null -> {
                    val r = recipe!!

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!r.imageUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = r.imageUrl,
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

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = r.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (r.cookingTime.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = r.cookingTime,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SmartCutColors.TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (r.ingredients.isNotEmpty()) {
                        val cuttableIngredients = r.ingredients.filter { it.cuttable }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ингредиенты",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (cuttableIngredients.isNotEmpty()) {
                                Text(
                                    text = "Нажмите ✂ для нарезки",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SmartCutColors.TextSecondary
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        r.ingredients.forEach { ingredient ->
                            if (ingredient.cuttable) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .clickable { selectedIngredient = ingredient.name },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
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
                                            if (ingredient.amount.isNotEmpty()) {
                                                Text(
                                                    text = ingredient.amount,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SmartCutColors.TextSecondary
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.ContentCut,
                                            contentDescription = "Нарезать",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.outline)
                                    )
                                    Text(
                                        text = if (ingredient.amount.isNotEmpty())
                                            "${ingredient.name} — ${ingredient.amount}"
                                        else ingredient.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SmartCutColors.TextSecondary
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
