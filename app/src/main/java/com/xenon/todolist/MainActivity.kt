package com.xenon.todolist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.view.WindowCompat
import com.xenon.todolist.ui.layouts.TodoListLayout // Assuming TodoListLayout will also need onOpenSettings
import com.xenon.todolist.ui.theme.ScreenEnvironment
import com.xenon.todolist.ui.values.LargeCornerRadius
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TodoViewModel
// If HazeState is only used within TodolistApp and not directly here,
// its import might not be strictly necessary at this top level.
// import dev.chrisbanes.haze.HazeState
// import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi


class MainActivity : ComponentActivity() {

    private val todoViewModel: TodoViewModel by viewModels()
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
            activeThemeForMainActivity = 2 // Default to system if out of bounds
        }
        coverEnabledState = sharedPreferenceManager.coverThemeEnabled

        setContent {
            val containerSize = LocalWindowInfo.current.containerSize
            val applyCoverTheme = sharedPreferenceManager.isCoverThemeApplied(containerSize)
            val currentContext = LocalContext.current

            ScreenEnvironment(
                themePreference = activeThemeForMainActivity,
                coverTheme = applyCoverTheme
            ) { layoutType, isLandscape ->

                val screenBackgroundColor = if (layoutType == LayoutType.COVER) Color.Black
                else colorScheme.background

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(screenBackgroundColor),
                    color = screenBackgroundColor
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (layoutType == LayoutType.COVER) {
                                    Modifier // No clipping or specific background for COVER layout at this level
                                } else {
                                    Modifier.clip(
                                        RoundedCornerShape(
                                            topStart = LargeCornerRadius,
                                            topEnd = LargeCornerRadius
                                        )
                                    )
                                        .background(colorScheme.surfaceContainer)
                                }
                            )
                    ) {
                        TodolistApp(
                            viewModel = todoViewModel,
                            layoutType = layoutType,
                            isLandscape = isLandscape,
                            onOpenSettings = {
                                val intent = Intent(currentContext, SettingsActivity::class.java)
                                currentContext.startActivity(intent)
                            }
                        )
                    }
                }
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
    viewModel: TodoViewModel,
    layoutType: LayoutType,
    isLandscape: Boolean,
    onOpenSettings: () -> Unit
) {
    TodoListLayout(
        viewModel = viewModel,
        isLandscape = isLandscape,
        layoutType = layoutType,
        onOpenSettings = onOpenSettings,
        modifier = Modifier.fillMaxSize()
    )
}