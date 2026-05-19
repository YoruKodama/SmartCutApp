package com.example.smartcutapp.presentation.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcutapp.R
import com.example.smartcutapp.app.ui.theme.SmartCutColors

data class RecipeUi(
    val id: Int,
    val name: String,
    val time: String,
)

data class CutModeUi(
    val label: String,
    val description: String
)

private val tempRecipeOfDay = RecipeUi(
    id = 1,
    name = "Салат Цезарь",
    time = "30 мин",
)

private val tempCutModes = listOf(
    CutModeUi("Кубиками", "Размер кубика"),
    CutModeUi("Слайсами", "Толщина слайса"),
)

private val tempRecentRecipes = listOf(
    RecipeUi(1, "Цезарь", "4 ингр"),
    RecipeUi(2, "Греческий", "6 ингр"),
    RecipeUi(3, "Оливье", "8 ингр"),
    RecipeUi(4, "Капрезе", "3 ингр"),
)

@Composable
fun MainScreen(navController: NavController) {
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
                RecipeOfDayCard(
                    recipe = tempRecipeOfDay,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
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
                            onClick = { navController.navigate("blade_settings") }
                        )
                    }
                }
            }

            item {
                SectionTitle(
                    title = "Недавние рецепты",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            items(tempRecentRecipes) { recipe ->
                RecentRecipeCard(
                    recipe = recipe,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    onClick = { navController.navigate("recipe_detail/${recipe.id}") }
                )
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
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Меню",
            tint = if (darkTheme) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onPrimary
        )
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
private fun RecipeOfDayCard(recipe: RecipeUi, modifier: Modifier = Modifier) {
    val darkTheme = isSystemInDarkTheme()
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = recipe.time,
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
private fun RecentRecipeCard(
    recipe: RecipeUi,
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
                Icon(
                    painter = painterResource(id = R.drawable.salad_svgrepo_com__1_),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = recipe.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmartCutColors.TextSecondary
                )
            }
        }
    }
}