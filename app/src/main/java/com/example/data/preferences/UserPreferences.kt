package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "akalabya_preferences")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class JournalFont {
    SERIF, SANS_SERIF, MONOSPACE
}

enum class AppPalette(
    val title: String,
    val subtitle: String,
    val primaryHex: Long,
    val secondaryHex: Long
) {
    FOREST_SANCTUARY(
        title = "Forest Sanctuary",
        subtitle = "Earthy Sage & Warm Terracotta",
        primaryHex = 0xFF2C3E35,
        secondaryHex = 0xFFC05C46
    ),
    MIDNIGHT_AMBER(
        title = "Midnight Amber",
        subtitle = "Obsidian Slate & Warm Gold",
        primaryHex = 0xFF7A5200,
        secondaryHex = 0xFF9A551E
    ),
    NORDIC_INDIGO(
        title = "Nordic Indigo",
        subtitle = "Deep Alpine Navy & Arctic Ice",
        primaryHex = 0xFF1D3557,
        secondaryHex = 0xFF457B9D
    ),
    ROSEWOOD_CASHMERE(
        title = "Rosewood & Cashmere",
        subtitle = "Warm Ivory & Dusky Plum",
        primaryHex = 0xFF6B2D44,
        secondaryHex = 0xFFA25772
    ),
    KYOTO_MATCHA(
        title = "Kyoto Matcha",
        subtitle = "Zen Olive & Antique Bronze",
        primaryHex = 0xFF384E2E,
        secondaryHex = 0xFF7A6532
    )
}

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val journalFont: JournalFont = JournalFont.SERIF,
    val appPalette: AppPalette = AppPalette.FOREST_SANCTUARY,
    val autoSaveIntervalMs: Long = 1000L,
    val biometricLockEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = true,
    val offlineUserId: String = "local_guest_user",
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20, // Default 8:00 PM
    val reminderMinute: Int = 0
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val JOURNAL_FONT = stringPreferencesKey("journal_font")
        val APP_PALETTE = stringPreferencesKey("app_palette")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val CLOUD_SYNC = booleanPreferencesKey("cloud_sync")
        val OFFLINE_USER_ID = stringPreferencesKey("offline_user_id")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val appPreferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { preferences ->
        val themeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val fontString = preferences[PreferencesKeys.JOURNAL_FONT] ?: JournalFont.SERIF.name
        val paletteString = preferences[PreferencesKeys.APP_PALETTE] ?: AppPalette.FOREST_SANCTUARY.name
        val biometricLock = preferences[PreferencesKeys.BIOMETRIC_LOCK] ?: false
        val cloudSync = preferences[PreferencesKeys.CLOUD_SYNC] ?: true
        val offlineUserId = preferences[PreferencesKeys.OFFLINE_USER_ID] ?: "local_guest_user"
        val reminderEnabled = preferences[PreferencesKeys.REMINDER_ENABLED] ?: false
        val reminderHour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 20
        val reminderMinute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0

        AppPreferences(
            themeMode = runCatching { ThemeMode.valueOf(themeString) }.getOrDefault(ThemeMode.SYSTEM),
            journalFont = runCatching { JournalFont.valueOf(fontString) }.getOrDefault(JournalFont.SERIF),
            appPalette = runCatching { AppPalette.valueOf(paletteString) }.getOrDefault(AppPalette.FOREST_SANCTUARY),
            biometricLockEnabled = biometricLock,
            cloudSyncEnabled = cloudSync,
            offlineUserId = offlineUserId,
            reminderEnabled = reminderEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
    }

    val themeModeFlow: Flow<ThemeMode> = appPreferencesFlow.map { it.themeMode }
    val fontFlow: Flow<JournalFont> = appPreferencesFlow.map { it.journalFont }
    val paletteFlow: Flow<AppPalette> = appPreferencesFlow.map { it.appPalette }
    val biometricLockFlow: Flow<Boolean> = appPreferencesFlow.map { it.biometricLockEnabled }
    val reminderFlow: Flow<Triple<Boolean, Int, Int>> = appPreferencesFlow.map { 
        Triple(it.reminderEnabled, it.reminderHour, it.reminderMinute) 
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        updateThemeMode(themeMode)
    }

    suspend fun setFont(font: JournalFont) {
        updateJournalFont(font)
    }

    suspend fun setPalette(palette: AppPalette) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_PALETTE] = palette.name
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun updateJournalFont(font: JournalFont) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.JOURNAL_FONT] = font.name
        }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_LOCK] = enabled
        }
    }

    suspend fun setCloudSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_SYNC] = enabled
        }
    }

    suspend fun setOfflineUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFLINE_USER_ID] = userId
        }
    }

    suspend fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_ENABLED] = enabled
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
        }
    }
}
