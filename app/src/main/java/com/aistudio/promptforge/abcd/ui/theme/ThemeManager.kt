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
import java.util.UUID

private val Context.perficioThemeDataStore: DataStore<Preferences> by preferencesDataStore(name = "perficio_theme_prefs")

class ThemeManager(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val dataStore = context.applicationContext.perficioThemeDataStore
    private val legacyPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadInitialTheme())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _glowEffectsEnabled = MutableStateFlow(legacyPrefs.getBoolean(KEY_GLOW_ENABLED, true))
    val glowEffectsEnabled: StateFlow<Boolean> = _glowEffectsEnabled.asStateFlow()

    private val _customThemes = MutableStateFlow(loadInitialCustomThemes())
    val customThemes: StateFlow<List<CustomThemeConfig>> = _customThemes.asStateFlow()

    private val _activeCustomThemeId = MutableStateFlow(loadInitialActiveCustomThemeId())
    val activeCustomThemeId: StateFlow<String> = _activeCustomThemeId.asStateFlow()

    private val _activeCustomTheme = MutableStateFlow(resolveActiveCustomTheme())
    val activeCustomTheme: StateFlow<CustomThemeConfig> = _activeCustomTheme.asStateFlow()

    init {
        // Observe DataStore for cross-process or background theme preference updates
        scope.launch {
            dataStore.data
                .catch { emit(emptyPreferences()) }
                .collectLatest { prefs ->
                    val savedTheme = prefs[PREF_THEME_MODE]
                    if (savedTheme != null) {
                        try {
                            val parsed = AppThemeMode.valueOf(savedTheme)
                            _themeMode.value = if (parsed == AppThemeMode.SYSTEM) AppThemeMode.OBSIDIAN else parsed
                        } catch (_: Exception) {
                            _themeMode.value = AppThemeMode.OBSIDIAN
                        }
                    }
                    val savedGlow = prefs[PREF_GLOW_ENABLED]
                    if (savedGlow != null) {
                        _glowEffectsEnabled.value = savedGlow
                    }
                    val savedThemesJson = prefs[PREF_CUSTOM_THEMES_JSON]
                    if (savedThemesJson != null && savedThemesJson.isNotBlank()) {
                        val parsedThemes = CustomThemeConfig.listFromJson(savedThemesJson)
                        if (parsedThemes.isNotEmpty()) {
                            _customThemes.value = parsedThemes
                        }
                    }
                    val savedActiveCustomId = prefs[PREF_ACTIVE_CUSTOM_ID]
                    if (savedActiveCustomId != null) {
                        _activeCustomThemeId.value = savedActiveCustomId
                    }
                    _activeCustomTheme.value = resolveActiveCustomTheme()
                }
        }
    }

    private fun loadInitialTheme(): AppThemeMode {
        val savedName = legacyPrefs.getString(KEY_THEME_MODE, null)
        return if (savedName != null) {
            try {
                val parsed = AppThemeMode.valueOf(savedName)
                if (parsed == AppThemeMode.SYSTEM) AppThemeMode.OBSIDIAN else parsed
            } catch (_: Exception) {
                AppThemeMode.OBSIDIAN
            }
        } else {
            // First launch defaults to Perficio Obsidian
            AppThemeMode.OBSIDIAN
        }
    }

    private fun loadInitialCustomThemes(): List<CustomThemeConfig> {
        val savedJson = legacyPrefs.getString(KEY_CUSTOM_THEMES_JSON, null)
        if (savedJson != null && savedJson.isNotBlank()) {
            val list = CustomThemeConfig.listFromJson(savedJson)
            if (list.isNotEmpty()) return list
        }
        return CustomThemeConfig.StarterCustomThemes
    }

    private fun loadInitialActiveCustomThemeId(): String {
        return legacyPrefs.getString(KEY_ACTIVE_CUSTOM_ID, null)
            ?: _customThemes.value.firstOrNull()?.id
            ?: CustomThemeConfig.StarterCustomThemes.first().id
    }

    private fun resolveActiveCustomTheme(): CustomThemeConfig {
        val currentId = _activeCustomThemeId.value
        return _customThemes.value.find { it.id == currentId }
            ?: _customThemes.value.firstOrNull()
            ?: CustomThemeConfig.StarterCustomThemes.first()
    }

    fun setThemeMode(mode: AppThemeMode) {
        val resolved = if (mode == AppThemeMode.SYSTEM) AppThemeMode.OBSIDIAN else mode
        _themeMode.value = resolved
        legacyPrefs.edit().putString(KEY_THEME_MODE, resolved.name).apply()
        scope.launch {
            dataStore.edit { prefs ->
                prefs[PREF_THEME_MODE] = resolved.name
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

    fun setActiveCustomThemeId(id: String) {
        _activeCustomThemeId.value = id
        _activeCustomTheme.value = resolveActiveCustomTheme()
        legacyPrefs.edit().putString(KEY_ACTIVE_CUSTOM_ID, id).apply()
        scope.launch {
            dataStore.edit { prefs ->
                prefs[PREF_ACTIVE_CUSTOM_ID] = id
            }
        }
    }

    fun saveCustomTheme(config: CustomThemeConfig) {
        val currentList = _customThemes.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            currentList[index] = config
        } else {
            currentList.add(config)
        }
        _customThemes.value = currentList
        _activeCustomThemeId.value = config.id
        _activeCustomTheme.value = config

        val jsonStr = CustomThemeConfig.listToJson(currentList)
        legacyPrefs.edit()
            .putString(KEY_CUSTOM_THEMES_JSON, jsonStr)
            .putString(KEY_ACTIVE_CUSTOM_ID, config.id)
            .apply()

        scope.launch {
            dataStore.edit { prefs ->
                prefs[PREF_CUSTOM_THEMES_JSON] = jsonStr
                prefs[PREF_ACTIVE_CUSTOM_ID] = config.id
            }
        }
    }

    fun deleteCustomTheme(id: String) {
        val currentList = _customThemes.value.filterNot { it.id == id }
        val finalList = if (currentList.isEmpty()) CustomThemeConfig.StarterCustomThemes else currentList
        _customThemes.value = finalList

        if (_activeCustomThemeId.value == id) {
            val newActive = finalList.first()
            _activeCustomThemeId.value = newActive.id
            _activeCustomTheme.value = newActive
        } else {
            _activeCustomTheme.value = resolveActiveCustomTheme()
        }

        val jsonStr = CustomThemeConfig.listToJson(finalList)
        legacyPrefs.edit()
            .putString(KEY_CUSTOM_THEMES_JSON, jsonStr)
            .putString(KEY_ACTIVE_CUSTOM_ID, _activeCustomThemeId.value)
            .apply()

        scope.launch {
            dataStore.edit { prefs ->
                prefs[PREF_CUSTOM_THEMES_JSON] = jsonStr
                prefs[PREF_ACTIVE_CUSTOM_ID] = _activeCustomThemeId.value
            }
        }
    }

    fun duplicateCustomTheme(id: String): CustomThemeConfig {
        val source = _customThemes.value.find { it.id == id } ?: _activeCustomTheme.value
        val newCopy = source.copy(
            id = UUID.randomUUID().toString(),
            name = "${source.name} (Copy)",
            createdAt = System.currentTimeMillis()
        )
        saveCustomTheme(newCopy)
        return newCopy
    }

    fun resetCustomTheme(id: String) {
        val target = _customThemes.value.find { it.id == id } ?: return
        val resetTheme = CustomThemeConfig(
            id = target.id,
            name = target.name,
            backgroundColorHex = 0xFF090B10,
            surfaceColorHex = 0xFF11151F,
            surfaceElevatedColorHex = 0xFF171D2B,
            primaryColorHex = 0xFF00B4A0,
            secondaryColorHex = 0xFF38BDF8,
            successColorHex = 0xFF10B981,
            warningColorHex = 0xFFF59E0B,
            errorColorHex = 0xFFEF4444,
            borderColorHex = 0xFF1F293D,
            textPrimaryColorHex = 0xFFF1F5F9,
            textSecondaryColorHex = 0xFF94A3B8,
            cornerRadiusDp = 12,
            glowIntensity = 0.5f,
            surfaceStyle = SurfaceStyle.ELEVATED,
            backgroundEffect = BackgroundEffect.SUBTLE_GRADIENT,
            themeIntensity = 0.75f,
            fontScale = 1.0f,
            density = ThemeDensity.COMFORTABLE
        )
        saveCustomTheme(resetTheme)
    }

    fun resetToDefault() {
        setThemeMode(AppThemeMode.OBSIDIAN)
    }

    fun resolveCurrentTokens(allowGlow: Boolean = true): AppThemeTokens {
        return resolveTokens(_themeMode.value, _activeCustomTheme.value, allowGlow)
    }

    companion object {
        private const val PREFS_NAME = "perficio_theme_preferences"
        private const val KEY_THEME_MODE = "perficio_theme_mode"
        private const val KEY_GLOW_ENABLED = "perficio_glow_enabled"
        private const val KEY_CUSTOM_THEMES_JSON = "perficio_custom_themes_json"
        private const val KEY_ACTIVE_CUSTOM_ID = "perficio_active_custom_id"

        val PREF_THEME_MODE = stringPreferencesKey("perficio_theme_mode")
        val PREF_GLOW_ENABLED = booleanPreferencesKey("perficio_glow_enabled")
        val PREF_CUSTOM_THEMES_JSON = stringPreferencesKey("perficio_custom_themes_json")
        val PREF_ACTIVE_CUSTOM_ID = stringPreferencesKey("perficio_active_custom_id")

        fun resolveTokens(
            mode: AppThemeMode,
            customConfig: CustomThemeConfig? = null,
            allowGlow: Boolean = true
        ): AppThemeTokens {
            val baseTokens = when (mode) {
                AppThemeMode.OBSIDIAN, AppThemeMode.SYSTEM -> ObsidianThemeTokens
                AppThemeMode.DARK -> DarkThemeTokens
                AppThemeMode.LIGHT -> LightThemeTokens
                AppThemeMode.NEON -> NeonThemeTokens
                AppThemeMode.CYBERPUNK -> CyberpunkThemeTokens
                AppThemeMode.CUSTOM -> customConfig?.toTokens() ?: ObsidianThemeTokens
            }
            return if (!allowGlow && baseTokens.isGlowEnabled) {
                baseTokens.copy(isGlowEnabled = false)
            } else {
                baseTokens
            }
        }
    }
}
