package com.xenon.todolist

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.unit.IntSize
import androidx.core.content.edit
import kotlin.math.max
import kotlin.math.min

class SharedPreferenceManager(context: Context) {

    private val prefsName = "TodoListPrefs"
    private val themeKey = "app_theme" // 0: Light, 1: Dark, 2 (or other): System
    private val coverThemeEnabledKey = "cover_theme_enabled"
    private val coverDisplayDimension1Key = "cover_display_dimension_1"
    private val coverDisplayDimension2Key = "cover_display_dimension_2"

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    var theme: Int
        get() = sharedPreferences.getInt(themeKey, 2)
        set(value) = sharedPreferences.edit { putInt(themeKey, value) }

    val themeFlag: Array<Int> = arrayOf(
        AppCompatDelegate.MODE_NIGHT_NO,    // Index 0: Light
        AppCompatDelegate.MODE_NIGHT_YES,   // Index 1: Dark
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // Index 2: System
    )

    var coverThemeEnabled: Boolean
        get() = sharedPreferences.getBoolean(coverThemeEnabledKey, false)
        set(value) = sharedPreferences.edit { putBoolean(coverThemeEnabledKey, value) }

    var coverDisplaySize: IntSize
        get() {
            val dim1 = sharedPreferences.getInt(coverDisplayDimension1Key, 0)
            val dim2 = sharedPreferences.getInt(coverDisplayDimension2Key, 0)

            return IntSize(dim1, dim2)
        }
        set(value) {
            sharedPreferences.edit {
                putInt(coverDisplayDimension1Key, min(value.width, value.height))
                putInt(coverDisplayDimension2Key, max(value.width, value.height))
            }
        }

    fun isCoverThemeApplied(currentDisplaySize: IntSize): Boolean {
        if (!coverThemeEnabled) return false

        val storedDimension1 = sharedPreferences.getInt(coverDisplayDimension1Key, 0)
        val storedDimension2 = sharedPreferences.getInt(coverDisplayDimension2Key, 0)

        if (storedDimension1 == 0 || storedDimension2 == 0) return false

        val currentDimension1 = min(currentDisplaySize.width, currentDisplaySize.height)
        val currentDimension2 = max(currentDisplaySize.width, currentDisplaySize.height)

        return currentDimension1 == storedDimension1 && currentDimension2 == storedDimension2
    }
}