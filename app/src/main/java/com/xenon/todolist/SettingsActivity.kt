package com.xenon.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.lifecycle.ViewModelProvider
import com.xenon.todolist.ui.layouts.SettingsLayout
import com.xenon.todolist.ui.theme.ScreenEnvironment
import com.xenon.todolist.viewmodel.SettingsViewModel
import kotlin.jvm.java

class SettingsActivity : ComponentActivity() {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModel.SettingsViewModelFactory(application)
        )[SettingsViewModel::class.java]

        enableEdgeToEdge()

        setContent {

            val activeNightMode by settingsViewModel.activeNightModeFlag.collectAsState()
            LaunchedEffect(activeNightMode) {
                AppCompatDelegate.setDefaultNightMode(activeNightMode)
            }

            val persistedAppThemeIndex by settingsViewModel.persistedThemeIndex.collectAsState()

            val coverThemeEnabled by settingsViewModel.enableCoverTheme.collectAsState()
            val containerSize = LocalWindowInfo.current.containerSize
            val applyCoverTheme = remember(containerSize, coverThemeEnabled) {
                settingsViewModel.applyCoverTheme(containerSize)
            }

            val currentLanguage by settingsViewModel.currentLanguage.collectAsState()
            LaunchedEffect(currentLanguage) {
            }

            ScreenEnvironment(persistedAppThemeIndex, applyCoverTheme) { layoutType, isLandscape ->

            SettingsLayout(
                    onNavigateBack = { finish() },
                    viewModel = settingsViewModel,
                    isLandscape = isLandscape,
                    layoutType = layoutType
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settingsViewModel.updateCurrentLanguage()
    }
}