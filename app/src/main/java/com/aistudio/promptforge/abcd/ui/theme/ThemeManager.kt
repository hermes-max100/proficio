package com.aistudio.promptforge.abcd.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "autoflow_theme_prefs")

class ThemeManager(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val dataStore = context.applicationContext.themeDataStore
    private val legacyPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadInitialTheme())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _glowEffectsEnabled = MutableStateFlow(legacyPrefs.getBoolean(KEY_GLOW_ENABLED, true))
    val glowEffectsEnabled: StateFlow<Boolean> = _glowEffectsEnabled.asStateFlow()

    init {
        // Observe DataStore for cross-process or background theme preference updates
        scope.launch {
            dataStore.data
                .catch { emit(emptyPreferences()) }
                .collectLatest { prefs ->
                    val savedTheme = prefs[PREF_THEME_MODE]
                    if (savedTheme != null) {
                        try {
                            _themeMode.value = AppThemeMode.valueOf(savedTheme)
                        } catch (_: Exception) {
                            _themeMode.value = AppThemeMode.SYSTEM
                        }
                    }
                    val savedGlow = prefs[PREF_GLOW_ENABLED]
                    if (savedGlow != null) {
                        _glowEffectsEnabled.value = savedGlow
                    }
                }
        }
    }

    private fun loadInitialTheme(): AppThemeMode {
        val savedName = legacyPrefs.getString(KEY_THEME_MODE, null)
        return if (savedName != null) {
            try {
                AppThemeMode.valueOf(savedName)
            } catch (_: Exception) {
                AppThemeMode.SYSTEM
            }
        } else {
            // First launch defaults to SYSTEM (following Android light/dark setting)
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        legacyPrefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        scope.launch {
            dataStore.edit { prefs ->
                prefs[PREF_THEME_MODE] = mode.name
            }
        }
    }

    fun setGlowEffectsEnabled(enabled: Boolean) {
        _glowEffectsEnabled.value = enabled
        legacyPrefs.edit().putBoolean(KEY_GLOW_ENABLED, enabled).apply()
        scope.launch {
            dataStore.edit { prefs ->
                prefs[PREF_GLOW_ENABLED] = enabled
            }
        }
    }

    fun resetToSystemDefault() {
        setThemeMode(AppThemeMode.SYSTEM)
    }

    companion object {
        private const val PREFS_NAME = "autoflow_theme_preferences"
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_GLOW_ENABLED = "app_glow_enabled"

        val PREF_THEME_MODE = stringPreferencesKey("autoflow_theme_mode")
        val PREF_GLOW_ENABLED = booleanPreferencesKey("autoflow_glow_enabled")

        fun resolveTokens(mode: AppThemeMode, isSystemDark: Boolean, allowGlow: Boolean = true): AppThemeTokens {
            val baseTokens = when (mode) {
                AppThemeMode.SYSTEM -> if (isSystemDark) DarkThemeTokens else LightThemeTokens
                AppThemeMode.DARK -> DarkThemeTokens
                AppThemeMode.LIGHT -> LightThemeTokens
                AppThemeMode.NEON -> NeonThemeTokens
                AppThemeMode.CYBERPUNK -> CyberpunkThemeTokens
            }
            return if (!allowGlow && baseTokens.isGlowEnabled) {
                baseTokens.copy(isGlowEnabled = false)
            } else {
                baseTokens
            }
        }
    }
}
