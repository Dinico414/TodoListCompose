package com.xenon.todolist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.core.view.WindowCompat
import com.xenon.todolist.ui.layouts.TodoListLayout
import com.xenon.todolist.ui.theme.ScreenEnvironment
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {

    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    private var lastAppliedTheme: Int = -1
    private var lastAppliedCoverThemeEnabled: Boolean =
        false // This tracks the setting value itself
    private var lastAppliedBlackedOutMode: Boolean = false

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        sharedPreferenceManager = SharedPreferenceManager(applicationContext)

        val initialThemePref = sharedPreferenceManager.theme
        // Store the raw setting value for change detection
        val initialCoverThemeEnabledSetting = sharedPreferenceManager.coverThemeEnabled
        val initialBlackedOutMode = sharedPreferenceManager.blackedOutModeEnabled

        updateAppCompatDelegateTheme(initialThemePref)

        lastAppliedTheme = initialThemePref
        lastAppliedCoverThemeEnabled = initialCoverThemeEnabledSetting // Store the raw setting
        lastAppliedBlackedOutMode = initialBlackedOutMode

        setContent {
            // val windowSizeClassValue = calculateWindowSizeClass(this) // Not directly used for this logic
            // val currentWidthSizeClass = windowSizeClassValue.widthSizeClass // Not directly used for this logic

            val currentContext = LocalContext.current
            val currentContainerSize = LocalWindowInfo.current.containerSize // Use LocalWindowInfo

            // Determine if cover theme should be ACTUALLY applied based on setting AND screen dimensions
            val applyCoverTheme =
                sharedPreferenceManager.isCoverThemeApplied(currentContainerSize) // Use currentContainerSize

            ScreenEnvironment(
                themePreference = lastAppliedTheme,
                coverTheme = applyCoverTheme, // Use the dynamically calculated value
                blackedOutModeEnabled = lastAppliedBlackedOutMode
            ) { layoutType, isLandscape ->
                TodolistApp(
                    viewModel = taskViewModel,
                    layoutType = layoutType,
                    isLandscape = isLandscape,
                    appSize = currentContainerSize, // Pass currentContainerSize
                    onOpenSettings = {
                        val intent = Intent(currentContext, SettingsActivity::class.java)
                        currentContext.startActivity(intent)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val currentThemePref = sharedPreferenceManager.theme
        val currentCoverThemeEnabledSetting =
            sharedPreferenceManager.coverThemeEnabled // Get the raw setting value
        val currentBlackedOutMode = sharedPreferenceManager.blackedOutModeEnabled

        // Check if any of the raw settings have changed
        if (currentThemePref != lastAppliedTheme || currentCoverThemeEnabledSetting != lastAppliedCoverThemeEnabled || // Compare with the stored raw setting
            currentBlackedOutMode != lastAppliedBlackedOutMode
        ) {
            if (currentThemePref != lastAppliedTheme) {
                updateAppCompatDelegateTheme(currentThemePref)
            }

            // Update the last applied raw settings
            lastAppliedTheme = currentThemePref
            lastAppliedCoverThemeEnabled = currentCoverThemeEnabledSetting
            lastAppliedBlackedOutMode = currentBlackedOutMode

            recreate() // Recreate if any setting changed
        }
    }

    private fun updateAppCompatDelegateTheme(themePref: Int) {
        if (themePref >= 0 && themePref < sharedPreferenceManager.themeFlag.size) {
            AppCompatDelegate.setDefaultNightMode(sharedPreferenceManager.themeFlag[themePref])
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}

@Composable
fun TodolistApp(
    viewModel: TaskViewModel,
    layoutType: LayoutType,
    isLandscape: Boolean,
    onOpenSettings: () -> Unit,
    appSize: IntSize,
) {
    TodoListLayout(
        viewModel = viewModel,
        isLandscape = isLandscape,
        layoutType = layoutType,
        onOpenSettings = onOpenSettings,
        modifier = Modifier.fillMaxSize(),
        appSize = appSize
    )
}

