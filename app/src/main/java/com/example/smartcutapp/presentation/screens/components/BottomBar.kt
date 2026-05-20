package com.example.smartcutapp.presentation.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartcutapp.R
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.presentation.navigation.Screen

data class BottomNavItem(
    val route: String,
    val iconRes: Int,
    val label: String
)

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Screen.Main.route, R.drawable.home_svgrepo_com_1, "Главная"),
        BottomNavItem(Screen.Recipes.route, R.drawable.notepad_svgrepo_com_1, "Рецепты"),
        BottomNavItem(Screen.Settings.route, R.drawable.settings_svgrepo_com_1, "Настройки"),
    )
    val navBackStack by navController.currentBackStackEntryAsState()
    val current = navBackStack?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        items.forEach { item ->
            NavigationBarItem(
                selected = current == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Screen.Main.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unselectedIconColor = SmartCutColors.TextSecondary,
                    unselectedTextColor = SmartCutColors.TextSecondary,
                )
            )
        }
    }
}