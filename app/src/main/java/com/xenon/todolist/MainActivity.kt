package com.xenon.todolist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.view.WindowCompat
import com.xenon.todolist.ui.layouts.TodoListLayout
import com.xenon.todolist.ui.theme.ScreenEnvironment
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {

    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private var activeThemeForMainActivity: Int = 2
    private var coverEnabledState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        sharedPreferenceManager = SharedPreferenceManager(applicationContext)

        activeThemeForMainActivity = sharedPreferenceManager.theme

        if (activeThemeForMainActivity >= 0 && activeThemeForMainActivity < sharedPreferenceManager.themeFlag.size) {
            AppCompatDelegate.setDefaultNightMode(sharedPreferenceManager.themeFlag[activeThemeForMainActivity])
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            activeThemeForMainActivity = 2
        }
        coverEnabledState = sharedPreferenceManager.coverThemeEnabled

        setContent {
            val containerSize = LocalWindowInfo.current.containerSize
            val applyCoverTheme = sharedPreferenceManager.isCoverThemeApplied(containerSize)
            val currentContext = LocalContext.current

            ScreenEnvironment(
                themePreference = activeThemeForMainActivity, coverTheme = applyCoverTheme
            ) { layoutType, isLandscape ->

                TodolistApp(
                    viewModel = taskViewModel,
                    layoutType = layoutType,
                    isLandscape = isLandscape,
                    onOpenSettings = {
                        val intent = Intent(currentContext, SettingsActivity::class.java)
                        currentContext.startActivity(intent)
                    })
            }
        }

    }

    override fun onResume() {
        super.onResume()

        val storedTheme = sharedPreferenceManager.theme
        if (activeThemeForMainActivity != storedTheme) {
            activeThemeForMainActivity = storedTheme
            recreate()
        }
        val applyCoverTheme = sharedPreferenceManager.coverThemeEnabled
        if (applyCoverTheme != coverEnabledState) {
            coverEnabledState = applyCoverTheme
            recreate()
        }
    }

}

@Composable
fun TodolistApp(
    viewModel: TaskViewModel,
    layoutType: LayoutType,
    isLandscape: Boolean,
    onOpenSettings: () -> Unit,
) {
    TodoListLayout(
        viewModel = viewModel,
        isLandscape = isLandscape,
        layoutType = layoutType,
        onOpenSettings = onOpenSettings,
        modifier = Modifier.fillMaxSize()
    )
}