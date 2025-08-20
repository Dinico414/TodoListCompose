package com.xenon.todolist.ui.theme

import android.content.res.Configuration
import android.view.Window
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.xenon.todolist.viewmodel.LayoutType

@Composable
fun ScreenEnvironment(
    themePreference: Int,
    coverTheme: Boolean,
    blackedOutModeEnabled: Boolean,
    content: @Composable (layoutType: LayoutType, isLandscape: Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    // This variable determines theme based on preference ONLY
    // val useDarkTheme = when (themePreference) { 
    //     0 -> false // Light
    //     1 -> true  // Dark
    //     else -> isSystemInDarkTheme() // System
    // }
    val useDynamicColor = true

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = this.maxWidth
        val screenHeight = this.maxHeight
        val dimensionForLayout = if (isLandscape) screenHeight else screenWidth

        val layoutType = when {
            coverTheme -> LayoutType.COVER
            dimensionForLayout < 320.dp -> LayoutType.SMALL
            dimensionForLayout < 600.dp -> LayoutType.COMPACT
            dimensionForLayout < 840.dp -> LayoutType.MEDIUM
            else -> LayoutType.EXPANDED
        }

        // This variable correctly determines if the theme should effectively be dark,
        // considering LayoutType.COVER
        val appIsDarkTheme = when {
            layoutType == LayoutType.COVER -> true
            else -> when (themePreference) {
                0 -> false
                1 -> true
                else -> isSystemInDarkTheme()
            }
        }

        TodolistTheme(
            darkTheme = appIsDarkTheme, // Use appIsDarkTheme here
            useBlackedOutDarkTheme = if (appIsDarkTheme) blackedOutModeEnabled else false, // Also use appIsDarkTheme here
            dynamicColor = useDynamicColor
        ) {
//            val insetController = WindowCompat.getInsetsController(window, window.decorView)

//            if (!view.isInEditMode && isLandscape) {
//                SideEffect {
//                    insetController.apply {
//                        hide(WindowInsetsCompat.Type.navigationBars())
//                        hide(WindowInsetsCompat.Type.navigationBars())
//                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//                    }
//                }
//            }
            content(layoutType, isLandscape)
        }
    }
}