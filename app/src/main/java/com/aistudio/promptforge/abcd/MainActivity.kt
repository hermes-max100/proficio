package com.aistudio.promptforge.abcd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aistudio.promptforge.abcd.data.PromptDatabase
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.ui.AppNavigation
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.MainViewModelFactory
import com.aistudio.promptforge.abcd.ui.theme.AutoFlowTheme
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {

    private val themeManager by lazy { ThemeManager(applicationContext) }
    private val database by lazy { PromptDatabase.getDatabase(this) }
    private val repository by lazy { PromptRepository(database.promptDao()) }
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(repository, themeManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeManager.themeMode.collectAsState()
            val activeCustomTheme by themeManager.activeCustomTheme.collectAsState()
            val glowEnabled by themeManager.glowEffectsEnabled.collectAsState()
            AutoFlowTheme(
                themeMode = themeMode,
                customConfig = activeCustomTheme,
                glowEnabled = glowEnabled
            ) {
                AppNavigation(viewModel)
            }
        }
    }
}
