package com.example.smartcutapp.presentation.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.presentation.navigation.Screen

data class CutModeUi(
    val label: String,
    val description: String
)

private val tempCutModes = listOf(
    CutModeUi("Кубиками", "Размер кубика"),
    CutModeUi("Слайсами", "Толщина слайса"),
)

@Composable
fun MainScreen(navController: NavController) {
    val viewModel: MainViewModel = viewModel()
    val recipes by viewModel.recipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecipes()
    }

    val recipeOfDay = recipes.firstOrNull()
    val recentRecipes = recipes.drop(1).take(4)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { MainHeader() }

            item {
                SectionTitle(
                    title = "Рецепт дня",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (recipeOfDay != null) {
                    RecipeOfDayCard(
                        recipe = recipeOfDay,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = { navController.navigate(Screen.RecipeDetail.createRoute(recipeOfDay.id)) }
                    )
                }
            }

            item {
                SectionTitle(
                    title = "Режимы нарезки",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tempCutModes.forEachIndexed { index, mode ->
                        CutModeCard(
                            mode = mode,
                            index = index + 1,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (index == 0) navController.navigate(Screen.BladeSettings.createRoute())
                                else navController.navigate(Screen.Slices.createRoute())
                            }
                        )
                    }
                }
            }

            item {
                SectionTitle(
                    title = "AI нарезка",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                AiScanCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = { navController.navigate(Screen.ProductScan.route) }
                )
            }

            if (recentRecipes.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Недавние рецепты",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                items(recentRecipes) { recipe ->
                    RecentRecipeCard(
                        recipe = recipe,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        onClick = { navController.navigate(Screen.RecipeDetail.createRoute(recipe.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainHeader() {
    val darkTheme = isSystemInDarkTheme()
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
            text = "SmartCut App",
            style = MaterialTheme.typography.titleLarge,
            color = if (darkTheme) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = SmartCutColors.TextSecondary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
private fun RecipeOfDayCard(recipe: Recipe, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Рецепт дня",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (darkTheme) SmartCutColors.TextSecondary
                    else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (darkTheme) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = recipe.cookingTime ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (darkTheme) SmartCutColors.TextSecondary
                    else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!recipe.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.salad_svgrepo_com__1_),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CutModeCard(
    mode: CutModeUi,
    index: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "0$index",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = mode.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = mode.description,
                style = MaterialTheme.typography.bodyMedium,
                color = SmartCutColors.TextSecondary
            )
        }
    }
}

@Composable
private fun AiScanCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔍", style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Распознать продукт",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ollama + ESP32-CAM подберёт режим нарезки автоматически",
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartCutColors.TextSecondary
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RecentRecipeCard(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!recipe.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.salad_svgrepo_com__1_),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = recipe.cookingTime ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmartCutColors.TextSecondary
                )
            }
        }
    }
}
