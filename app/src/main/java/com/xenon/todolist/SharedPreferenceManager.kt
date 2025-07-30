package com.xenon.todolist

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.unit.IntSize
import androidx.core.content.edit
import com.xenon.todolist.viewmodel.ThemeSetting // Import ThemeSetting
import com.xenon.todolist.viewmodel.classes.TaskItem
import com.xenon.todolist.viewmodel.classes.TodoItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat // Import for default date/time format
import java.util.Locale // Import for default date/time format
import kotlin.math.max
import kotlin.math.min

class SharedPreferenceManager(context: Context) {

    private val prefsName = "TodoListPrefs"
    private val themeKey = "app_theme"
    private val coverThemeEnabledKey = "cover_theme_enabled"
    private val coverDisplayDimension1Key = "cover_display_dimension_1"
    private val coverDisplayDimension2Key = "cover_display_dimension_2"
    private val taskListKey = "task_list_json"
    private val drawerTodoItemsKey = "drawer_todo_items_json"
    private val blackedOutModeKey = "blacked_out_mode_enabled"
    private val dateFormatKey = "date_format_key" // New key
    private val timeFormatKey = "time_format_key" // New key


    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // Default Date and Time Formats (Consider making these configurable or constants)
    // You can use system default or define your own application defaults.
    // Example: Using a common short date and time format
    private val defaultDateFormat = "yyyy-MM-dd"
    private val defaultTimeFormat = "HH:mm"


    var theme: Int
        get() = sharedPreferences.getInt(themeKey, ThemeSetting.SYSTEM.ordinal) // Default to System
        set(value) = sharedPreferences.edit { putInt(themeKey, value) }

    val themeFlag: Array<Int> = arrayOf(
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    var coverThemeEnabled: Boolean
        get() = sharedPreferences.getBoolean(coverThemeEnabledKey, false) // Default to false
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

    var taskItems: List<TaskItem>
        get() {
            val jsonString = sharedPreferences.getString(taskListKey, null)
            return if (jsonString != null) {
                try {
                    json.decodeFromString<List<TaskItem>>(jsonString)
                } catch (e: Exception) {
                    System.err.println("Error decoding task items: ${e.localizedMessage}")
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
        set(value) {
            try {
                val jsonString = json.encodeToString(value)
                sharedPreferences.edit { putString(taskListKey, jsonString) }
            } catch (e: Exception) {
                System.err.println("Error encoding task items: ${e.localizedMessage}")
            }
        }

    var drawerTodoItems: List<TodoItem>
        get() {
            val jsonString = sharedPreferences.getString(drawerTodoItemsKey, null)
            return if (jsonString != null) {
                try {
                    json.decodeFromString<List<TodoItem>>(jsonString)
                } catch (e: Exception) {
                    System.err.println("Error decoding drawer todo items: ${e.localizedMessage}")
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
        set(value) {
            try {
                val jsonString = json.encodeToString(value)
                sharedPreferences.edit { putString(drawerTodoItemsKey, jsonString) }
            } catch (e: Exception) {
                System.err.println("Error encoding drawer todo items: ${e.localizedMessage}")
            }
        }

    var blackedOutModeEnabled: Boolean
        get() = sharedPreferences.getBoolean(blackedOutModeKey, false)
        set(value) = sharedPreferences.edit { putBoolean(blackedOutModeKey, value) }

    var dateFormat: String
        get() = sharedPreferences.getString(dateFormatKey, defaultDateFormat) ?: defaultDateFormat
        set(value) = sharedPreferences.edit { putString(dateFormatKey, value) }

    var timeFormat: String
        get() = sharedPreferences.getString(timeFormatKey, defaultTimeFormat) ?: defaultTimeFormat
        set(value) = sharedPreferences.edit { putString(timeFormatKey, value) }


    fun isCoverThemeApplied(currentDisplaySize: IntSize): Boolean {
        if (!coverThemeEnabled) return false
        val storedDimension1 = sharedPreferences.getInt(coverDisplayDimension1Key, 0)
        val storedDimension2 = sharedPreferences.getInt(coverDisplayDimension2Key, 0)
        if (storedDimension1 == 0 || storedDimension2 == 0) return false
        val currentDimension1 = min(currentDisplaySize.width, currentDisplaySize.height)
        val currentDimension2 = max(currentDisplaySize.width, currentDisplaySize.height)
        return currentDimension1 == storedDimension1 && currentDimension2 == storedDimension2
    }

    fun clearSettings() {
        sharedPreferences.edit {
            putInt(themeKey, ThemeSetting.SYSTEM.ordinal)

            putBoolean(coverThemeEnabledKey, false)
            remove(coverDisplayDimension1Key)
            remove(coverDisplayDimension2Key)

            putBoolean(blackedOutModeKey, false)

            // Reset Date and Time format to defaults
            putString(dateFormatKey, defaultDateFormat)
            putString(timeFormatKey, defaultTimeFormat)
        }
    }
}
