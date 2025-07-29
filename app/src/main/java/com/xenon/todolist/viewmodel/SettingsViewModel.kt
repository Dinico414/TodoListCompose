package com.xenon.todolist.viewmodel

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.unit.IntSize
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xenon.todolist.R
import com.xenon.todolist.SharedPreferenceManager
import com.xenon.todolist.ui.res.LanguageOption
import kotlinx.coroutines.delay // Required for the delay before restarting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

// ThemeSetting and LayoutType enums would be here as in your original code
enum class ThemeSetting(val title: String, val nightModeFlag: Int) {
    LIGHT("Light", AppCompatDelegate.MODE_NIGHT_NO),
    DARK("Dark", AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM("System", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

enum class LayoutType {
    COVER, SMALL, COMPACT, MEDIUM, EXPANDED
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferenceManager = SharedPreferenceManager(application)
    val themeOptions = ThemeSetting.entries.toTypedArray()

    private val _blackedOutModeEnabled = MutableStateFlow(sharedPreferenceManager.blackedOutModeEnabled)
    val blackedOutModeEnabled: StateFlow<Boolean> = _blackedOutModeEnabled.asStateFlow()

    private val _persistedThemeIndexFlow = MutableStateFlow(sharedPreferenceManager.theme)
    val persistedThemeIndex: StateFlow<Int> = _persistedThemeIndexFlow.asStateFlow()

    private val _dialogPreviewThemeIndex = MutableStateFlow(sharedPreferenceManager.theme)
    val dialogPreviewThemeIndex: StateFlow<Int> = _dialogPreviewThemeIndex.asStateFlow()

    private val _currentThemeTitleFlow =
        MutableStateFlow(themeOptions.getOrElse(sharedPreferenceManager.theme) { themeOptions.first() }.title)
    val currentThemeTitle: StateFlow<String> = _currentThemeTitleFlow.asStateFlow()

    private val _currentLanguage = MutableStateFlow(getCurrentLocaleDisplayName())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _showThemeDialog = MutableStateFlow(false)
    val showThemeDialog: StateFlow<Boolean> = _showThemeDialog.asStateFlow()

    private val _showClearDataDialog = MutableStateFlow(false)
    val showClearDataDialog: StateFlow<Boolean> = _showClearDataDialog.asStateFlow()

    // --- Start: Added for Reset Settings ---
    private val _showResetSettingsDialog = MutableStateFlow(false)
    val showResetSettingsDialog: StateFlow<Boolean> = _showResetSettingsDialog.asStateFlow()

    private val _showCoverSelectionDialog = MutableStateFlow(false)
    val showCoverSelectionDialog: StateFlow<Boolean> = _showCoverSelectionDialog.asStateFlow()

    private val _enableCoverTheme = MutableStateFlow(sharedPreferenceManager.coverThemeEnabled)
    val enableCoverTheme: StateFlow<Boolean> = _enableCoverTheme.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    private val _availableLanguages = MutableStateFlow<List<LanguageOption>>(emptyList())
    val availableLanguages: StateFlow<List<LanguageOption>> = _availableLanguages.asStateFlow()

    private val _selectedLanguageTagInDialog = MutableStateFlow(getAppLocaleTag())
    val selectedLanguageTagInDialog: StateFlow<String> = _selectedLanguageTagInDialog.asStateFlow()


    val activeNightModeFlag: StateFlow<Int> = combine(
        _persistedThemeIndexFlow,
        _dialogPreviewThemeIndex,
        _showThemeDialog
    ) { persistedIndex, previewIndex, isDialogShowing ->
        val themeIndexToUse = if (isDialogShowing) {
            previewIndex
        } else {
            persistedIndex
        }
        themeOptions.getOrElse(themeIndexToUse) { themeOptions.first { it == ThemeSetting.SYSTEM } }
            .nightModeFlag
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = themeOptions.getOrElse(sharedPreferenceManager.theme) { themeOptions.first { it == ThemeSetting.SYSTEM } }.nightModeFlag
    )


    init {
        viewModelScope.launch {
            activeNightModeFlag.collect { nightMode ->
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }
        }

        viewModelScope.launch {
            _persistedThemeIndexFlow.collect { index ->
                _currentThemeTitleFlow.value = themeOptions.getOrElse(index) { themeOptions.first() }.title
            }
        }
        updateCurrentLanguage()
        prepareLanguageOptions()
        // The collect for _blackedOutModeEnabled in init seems redundant if it's not performing an action.
        // If it was for future use or logging, it's fine. Otherwise, it can be removed.
        // viewModelScope.launch {
        // _blackedOutModeEnabled.collect { newValue ->
        // // Action based on blackedOutModeEnabled change, if any
        // }
        // }
    }
    fun onThemeOptionSelectedInDialog(index: Int) {
        if (index >= 0 && index < themeOptions.size) {
            _dialogPreviewThemeIndex.value = index
            // Persisting immediately on selection in dialog might be desired, or only on "apply"
            // Current code updates _persistedThemeIndexFlow here too.
            _persistedThemeIndexFlow.value = index
        }
    }

    fun applySelectedTheme() {
        val indexToApply = _dialogPreviewThemeIndex.value
        if (indexToApply >= 0 && indexToApply < themeOptions.size) {
            sharedPreferenceManager.theme = indexToApply
            _persistedThemeIndexFlow.value = indexToApply // Ensure this is updated if not already
        }
        _showThemeDialog.value = false
    }

    fun onThemeSettingClicked() {
        _dialogPreviewThemeIndex.value = _persistedThemeIndexFlow.value // Sync preview with current
        _showThemeDialog.value = true
    }

    fun setBlackedOutEnabled(enabled: Boolean) {
        sharedPreferenceManager.blackedOutModeEnabled = enabled
        _blackedOutModeEnabled.value = enabled
    }

    fun dismissThemeDialog() {
        _showThemeDialog.value = false
        // Reset preview to currently persisted theme
        _dialogPreviewThemeIndex.value = sharedPreferenceManager.theme
        // Ensure persisted flow reflects the actual stored value upon dismissal without apply
        _persistedThemeIndexFlow.value = sharedPreferenceManager.theme
    }

    fun setCoverThemeEnabled(enabled: Boolean) {
        sharedPreferenceManager.coverThemeEnabled = enabled
        _enableCoverTheme.value = enabled
        if (!enabled) {
            // Optionally clear cover display size if cover theme is disabled
            // sharedPreferenceManager.coverDisplaySize = IntSize(0,0) // Or a specific reset
        }
    }

    fun onCoverThemeClicked() {
        _showCoverSelectionDialog.value = true
    }

    fun dismissCoverThemeDialog() {
        _showCoverSelectionDialog.value = false
    }

    fun saveCoverDisplayMetrics(displaySize: IntSize) {
        sharedPreferenceManager.coverDisplaySize = displaySize
        // No need to call setCoverThemeEnabled(true) here as it's just saving metrics
        // The enableCoverTheme state should be managed separately by its switch/toggle
        _enableCoverTheme.value = true // Assuming saving metrics implies enabling
        sharedPreferenceManager.coverThemeEnabled = true // Persist this assumption
        _showCoverSelectionDialog.value = false
    }

    fun applyCoverTheme(displaySize: IntSize): Boolean {
        return sharedPreferenceManager.isCoverThemeApplied(displaySize)
    }

    fun onClearDataClicked() {
        _showClearDataDialog.value = true
    }

    fun confirmClearData() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            try {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                @Suppress("DEPRECATION") // clearApplicationUserData is deprecated but functional for some cases
                val success = activityManager.clearApplicationUserData()

                if (success) {
                    // Reset theme in ViewModel and SharedPreferences
                    val defaultThemeIndex = themeOptions.indexOfFirst { it == ThemeSetting.SYSTEM }
                        .takeIf { it != -1 } ?: ThemeSetting.SYSTEM.ordinal
                    sharedPreferenceManager.theme = defaultThemeIndex
                    _persistedThemeIndexFlow.value = defaultThemeIndex
                    _dialogPreviewThemeIndex.value = defaultThemeIndex

                    // Reset blacked out mode
                    sharedPreferenceManager.blackedOutModeEnabled = false
                    _blackedOutModeEnabled.value = false

                    // Reset cover theme
                    sharedPreferenceManager.coverThemeEnabled = false
                    _enableCoverTheme.value = false
                    // sharedPreferenceManager.coverDisplaySize = IntSize(0,0) // Reset size

                    // Reset app locale for pre-Tiramisu
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        setAppLocale("")
                    }
                    updateCurrentLanguage() // Update language display

                    restartApplication(context)
                } else {
                    Toast.makeText(context, context.getString(R.string.error_clearing_data_failed), Toast.LENGTH_LONG).show()
                    openAppInfo(context)
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, context.getString(R.string.error_clearing_data_permission), Toast.LENGTH_LONG).show()
                openAppInfo(context)
                e.printStackTrace()
            } finally {
                _showClearDataDialog.value = false
            }
        }
    }

    fun dismissClearDataDialog() {
        _showClearDataDialog.value = false
    }

    // --- Start: Added for Reset Settings ---
    fun onResetSettingsClicked() {
        _showResetSettingsDialog.value = true
    }

    fun dismissResetSettingsDialog() {
        _showResetSettingsDialog.value = false
    }

    fun confirmResetSettings() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            // 1. Clear settings in SharedPreferences (this calls your new method)
            sharedPreferenceManager.clearSettings()

            // 2. Update ViewModel StateFlows to reflect default values
            // Theme (assuming ThemeSetting.SYSTEM.ordinal is your default index after clearSettings)
            val defaultThemeIndex = ThemeSetting.SYSTEM.ordinal
            _persistedThemeIndexFlow.value = defaultThemeIndex
            _dialogPreviewThemeIndex.value = defaultThemeIndex // Also reset preview

            // Blacked out mode
            _blackedOutModeEnabled.value = sharedPreferenceManager.blackedOutModeEnabled // Will be false

            // Cover theme
            _enableCoverTheme.value = sharedPreferenceManager.coverThemeEnabled // Will be false

            // 3. Reset app locale to system default (for pre-Tiramisu)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                setAppLocale("") // Empty string for system default
            }
            updateCurrentLanguage() // Update UI for language

            // 4. Dismiss the dialog
            _showResetSettingsDialog.value = false

            // 5. Inform user and restart application
            delay(1000) // Give user time to see the toast
            restartApplication(context)
        }
    }
    // --- End: Added for Reset Settings ---

    private fun getCurrentLocaleDisplayName(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        return if (appLocales.isEmpty || appLocales.get(0) == null) {
            getApplication<Application>().getString(R.string.system_default)
        } else {
            appLocales.get(0)!!.displayName
        }
    }

    private fun getAppLocaleTag(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        return if (appLocales.isEmpty) "" else appLocales.toLanguageTags()
    }

    fun updateCurrentLanguage() {
        _currentLanguage.value = getCurrentLocaleDisplayName()
        _selectedLanguageTagInDialog.value = getAppLocaleTag()
    }

    private fun prepareLanguageOptions() {
        val application = getApplication<Application>()
        val languages = mutableListOf(
            LanguageOption(application.getString(R.string.system_default), "")
        )
        // Example: Adding English and German
        val en = Locale("en")
        languages.add(LanguageOption(en.getDisplayName(en), en.toLanguageTag()))
        val de = Locale("de")
        languages.add(LanguageOption(de.getDisplayName(de), de.toLanguageTag()))
        // Add other languages as needed
        _availableLanguages.value = languages
    }

    fun onLanguageSettingClicked(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback or error message if the intent fails
                Toast.makeText(context, "Could not open language settings.", Toast.LENGTH_SHORT).show()
                _selectedLanguageTagInDialog.value = getAppLocaleTag()
                _showLanguageDialog.value = true
            }
        } else {
            _selectedLanguageTagInDialog.value = getAppLocaleTag()
            _showLanguageDialog.value = true
        }
    }

    fun onLanguageSelectedInDialog(localeTag: String) {
        _selectedLanguageTagInDialog.value = localeTag
    }

    fun applySelectedLanguage() {
        val context = getApplication<Application>()
        setAppLocale(_selectedLanguageTagInDialog.value)
        _showLanguageDialog.value = false
        updateCurrentLanguage() // Update displayed language immediately
        viewModelScope.launch {
            delay(1000) // Give user time to see toast
            restartApplication(context)
        }
    }

    private fun setAppLocale(localeTag: String) {
        val appLocale: LocaleListCompat = if (localeTag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList() // For system default
        } else {
            LocaleListCompat.forLanguageTags(localeTag)
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun dismissLanguageDialog() {
        _showLanguageDialog.value = false
        _selectedLanguageTagInDialog.value = getAppLocaleTag() // Reset to current app locale
    }

    fun openAppInfo(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open app info.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openImpressum(context: Context) {
        // Implement opening a URL or showing info for impressum
        Toast.makeText(context, "Impressum: xenonware.com/impressum", Toast.LENGTH_LONG).show()
        // Example for opening URL:
        // val url = "http://xenonware.com/impressum"
        // try {
        // val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        // addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // }
        // context.startActivity(intent)
        // } catch (e: Exception) {
        // Toast.makeText(context, "Could not open link.", Toast.LENGTH_SHORT).show()
        // }
    }

    private fun restartApplication(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent?.component != null) {
            val mainIntent = Intent.makeRestartActivityTask(intent.component)
            // mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // makeRestartActivityTask already sets these
            context.startActivity(mainIntent)
            Process.killProcess(Process.myPid())
        } else {
            Toast.makeText(context, context.getString(R.string.error_restarting_app), Toast.LENGTH_LONG).show()
        }
    }

    class SettingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
