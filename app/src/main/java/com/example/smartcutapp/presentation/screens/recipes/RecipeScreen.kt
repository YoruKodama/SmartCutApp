package com.example.smartcutapp.presentation.screens.recipes

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.ui.platform.LocalContext
import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.domain.usecase.RecipePlanner
import com.example.smartcutapp.presentation.components.AppHeader
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcutapp.R
import com.example.smartcutapp.data.remote.api.ApiClient
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.presentation.navigation.Screen
import com.example.smartcutapp.ui.theme.LocalDarkTheme


@Composable
fun RecipesScreen(navController: NavController) {
    val darkTheme = LocalDarkTheme.current
    val viewModel: RecipesViewModel = viewModel()
    var searchQuery by remember { mutableStateOf("") }

    val recipes by viewModel.recipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val offline by viewModel.offline.collectAsState()
    val message by viewModel.message.collectAsState()

    var onlyMyAttachments by remember { mutableStateOf(false) }
    val myAttachments = remember { PreferencesManager.availableAttachments }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val text = viewModel.exportJson()
            val written = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) } != null
            }.getOrDefault(false)
            viewModel.showMessage(
                if (written) "Экспортировано рецептов: ${viewModel.exportedCount()}" else "Не удалось сохранить файл"
            )
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            }.getOrNull()
            if (text == null) viewModel.showMessage("Не удалось прочитать файл") else viewModel.importJson(text)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadRecipes()
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    val filtered = recipes.filter {
        it.name.contains(searchQuery, ignoreCase = true) &&
            (!onlyMyAttachments || RecipePlanner.matchesAttachments(it, myAttachments))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { navController.navigate(Screen.AiRecipe.route) },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "AI помощник",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateRecipe.createRoute()) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Добавить рецепт",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            RecipesHeader(
                onExport = { exportLauncher.launch("smartcut-recipes.json") },
                onImport = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }
            )

            if (offline) {
                Text(
                    text = "Нет связи с сервером — показаны сохранённые на телефоне рецепты",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Найти рецепт...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = SmartCutColors.TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            FilterChip(
                selected = onlyMyAttachments,
                onClick = { onlyMyAttachments = !onlyMyAttachments },
                label = { Text("Под мои насадки") },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { navController.navigate(Screen.RecipeDetail.createRoute(recipe.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipesHeader(onExport: () -> Unit, onImport: () -> Unit) {
    AppHeader(title = "Рецепты") {
        IconButton(onClick = onImport) {
            Icon(Icons.Filled.FileDownload, contentDescription = "Импорт рецептов из файла")
        }
        IconButton(onClick = onExport) {
            Icon(Icons.Filled.FileUpload, contentDescription = "Экспорт рецептов в файл")
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
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
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!recipe.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ApiClient.resolveImageUrl(recipe.imageUrl),
                        contentDescription = recipe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.salad_svgrepo_com__1_),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
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