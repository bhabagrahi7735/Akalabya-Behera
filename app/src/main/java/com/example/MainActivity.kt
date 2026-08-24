package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import com.example.data.local.AkalabyaDatabase
import com.example.data.preferences.AppPalette
import com.example.data.preferences.JournalFont
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.JournalRepository
import com.example.ui.navigation.AkalabyaApp
import com.example.ui.theme.AkalabyaTheme

class MainActivity : FragmentActivity() {

    private lateinit var database: AkalabyaDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var journalRepository: JournalRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val context = applicationContext
        database = AkalabyaDatabase.getDatabase(context)
        userPreferencesRepository = UserPreferencesRepository(context)
        authRepository = AuthRepository(context, userPreferencesRepository)
        journalRepository = JournalRepository(context, database.journalDao())

        setContent {
            val themeMode by userPreferencesRepository.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val journalFont by userPreferencesRepository.fontFlow.collectAsState(initial = JournalFont.SERIF)
            val appPalette by userPreferencesRepository.paletteFlow.collectAsState(initial = AppPalette.FOREST_SANCTUARY)

            AkalabyaTheme(
                themeMode = themeMode,
                journalFont = journalFont,
                appPalette = appPalette
            ) {
                AkalabyaApp(
                    authRepository = authRepository,
                    journalRepository = journalRepository,
                    userPreferencesRepository = userPreferencesRepository
                )
            }
        }
    }
}
