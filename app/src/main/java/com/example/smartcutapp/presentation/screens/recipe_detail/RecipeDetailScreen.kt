package com.example.smartcutapp.presentation.screens.recipe_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.smartcutapp.presentation.screens.main.RecipeUi

private val tempRecipes = listOf(
    RecipeUi(1, "Салат Цезарь", "4 ингр"),
    RecipeUi(2, "Греческий салат", "6 ингр"),
    RecipeUi(3, "Оливье", "8 ингр"),
    RecipeUi(4, "Капрезе", "3 ингр"),
    RecipeUi(5, "Нисуаз", "7 ингр"),
    RecipeUi(6, "Табуле", "5 ингр"),
)

private val tempIngredients = listOf(
    "Куриное филе",
    "Салат романо",
    "Пармезан",
    "Гренки",
)

@Composable
fun RecipeDetailScreen(navController: NavController, recipeId: Int) {
    val recipe = tempRecipes.find { it.id == recipeId } ?: return
    val darkTheme = isSystemInDarkTheme()

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
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onPrimary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
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

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = recipe.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmartCutColors.TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Ингредиенты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            tempIngredients.forEach { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = ingredient,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}