package com.aistudio.promptforge.abcd.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aistudio.promptforge.abcd.ui.screens.DashboardScreen
import com.aistudio.promptforge.abcd.ui.screens.EngineScreen
import com.aistudio.promptforge.abcd.ui.screens.PluginForgeScreen
import com.aistudio.promptforge.abcd.ui.screens.PromptForgeScreen
import com.aistudio.promptforge.abcd.ui.screens.PromptRepositoryScreen
import com.aistudio.promptforge.abcd.ui.screens.SkillForgeScreen
import com.aistudio.promptforge.abcd.ui.screens.ThemeBuilderScreen
import com.aistudio.promptforge.abcd.ui.screens.VaultScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object PromptRepository : Screen("prompt_repository", "Repo", Icons.Filled.AutoAwesome)
    object PromptForge : Screen("prompt_forge", "Prompts", Icons.Filled.Edit)
    object Engine : Screen("engine", "Engine", Icons.Filled.FlashOn)
    object SkillForge : Screen("skill_forge", "Skills", Icons.Filled.Psychology)
    object PluginForge : Screen("plugin_forge", "Plugins", Icons.Filled.Extension)
    object Vault : Screen("vault", "Vault", Icons.Filled.Inventory)
    object History : Screen("history", "History", Icons.Filled.History)
    object ImportForm : Screen("import_form", "Import", Icons.Filled.UploadFile)
    object ThemeBuilder : Screen("theme_builder", "Theme Studio", Icons.Filled.Palette)
}

val navItems = listOf(
    Screen.Dashboard,
    Screen.PromptForge,
    Screen.Engine,
    Screen.History,
    Screen.Vault
)

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_navigation")
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navItems.forEach { screen ->
                    NavigationBarItem(
                        modifier = Modifier.testTag("nav_item_${screen.route}"),
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, maxLines = 1) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel, navController) }
            composable(Screen.PromptRepository.route) { PromptRepositoryScreen(viewModel, navController) }
            composable(Screen.Engine.route) { EngineScreen(viewModel, navController) }
            composable(Screen.PromptForge.route) { PromptForgeScreen(viewModel, navController) }
            composable(Screen.SkillForge.route) { SkillForgeScreen(viewModel, navController) }
            composable(Screen.PluginForge.route) { PluginForgeScreen(viewModel, navController) }
            composable(Screen.Vault.route) { VaultScreen(viewModel, navController) }
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToPromptStudio = { navController.navigate(Screen.PromptForge.route) },
                    onNavigateToImport = { navController.navigate(Screen.ImportForm.route) }
                )
            }
            composable(Screen.ImportForm.route) {
                ImportFormScreen(
                    viewModel = viewModel,
                    onNavigateToVault = { navController.navigate(Screen.Vault.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) }
                )
            }
            composable(Screen.ThemeBuilder.route) {
                ThemeBuilderScreen(viewModel = viewModel, navController = navController)
            }
        }
    }
}
