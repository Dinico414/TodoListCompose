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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ThemeSetting(val title: String, val nightModeFlag: Int) {
    LIGHT("Light", AppCompatDelegate.MODE_NIGHT_NO),
    DARK("Dark", AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM("System", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

enum class LayoutType {
    COVER, SMALL, COMPACT, MEDIUM, EXPANDED
}

data class FormatOption(val displayName: String, val pattern: String)

private fun getCurrentDateTimeFormatted(pattern: String): String {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.format(Date())
    } catch (_: IllegalArgumentException) {
        "Preview unavailable"
    }
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

    private val _currentDateFormat = MutableStateFlow(sharedPreferenceManager.dateFormat)

    private val _currentTimeFormat = MutableStateFlow(sharedPreferenceManager.timeFormat)

    val currentFormattedDateTime: StateFlow<String> = combine(
        _currentDateFormat,
        _currentTimeFormat
    ) { datePattern, timePattern ->
        try {
            val now = Date()
            val sdfDate = SimpleDateFormat(datePattern, Locale.getDefault())
            val sdfTime = SimpleDateFormat(timePattern, Locale.getDefault())
            "${sdfDate.format(now)} ${sdfTime.format(now)}"
        } catch (_: Exception) {
            "Invalid format"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Preview"
    )


    private val _showDateTimeFormatDialog = MutableStateFlow(false)
    val showDateTimeFormatDialog: StateFlow<Boolean> = _showDateTimeFormatDialog.asStateFlow()

    val availableDateFormats = listOf(
        FormatOption(
            displayName = "System Default (${getCurrentDateTimeFormatted(getSystemShortDatePattern())})",
            pattern = getSystemShortDatePattern()
        ),
        FormatOption("YYYY-MM-DD (${getCurrentDateTimeFormatted("yyyy-MM-dd")})", "yyyy-MM-dd"),
        FormatOption("DD/MM/YYYY (${getCurrentDateTimeFormatted("dd/MM/yyyy")})", "dd/MM/yyyy"),
        FormatOption("MM/DD/YYYY (${getCurrentDateTimeFormatted("MM/dd/yyyy")})", "MM/dd/yyyy"),
        FormatOption("DD.MM.YYYY (${getCurrentDateTimeFormatted("dd.MM.yyyy")})", "dd.MM.yyyy"),
    )

    private fun getSystemShortDatePattern(): String {
        return try {
            val dateTimeInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
            if (dateTimeInstance is SimpleDateFormat) {
                dateTimeInstance.toPattern().split(" ").firstOrNull() ?: "yyyy-MM-dd"
            } else {
                "yyyy-MM-dd"
            }
        } catch (_: Exception) {
            "yyyy-MM-dd"
        }
    }

    val systemShortTimePattern: String = getSystemShortTimePatternInternal()

    private fun getSystemShortTimePatternInternal(): String {
        return try {
            val dateTimeInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
            if (dateTimeInstance is SimpleDateFormat) {
                val parts = dateTimeInstance.toPattern().split(" ")
                if (parts.size > 1) parts.getOrNull(1) ?: "HH:mm" else "HH:mm"
            } else {
                "HH:mm"
            }
        } catch (_: Exception) {
            "HH:mm"
        }
    }

    private val _selectedDateFormatInDialog = MutableStateFlow(sharedPreferenceManager.dateFormat)
    val selectedDateFormatInDialog: StateFlow<String> = _selectedDateFormatInDialog.asStateFlow()

    private val _selectedTimeFormatInDialog = MutableStateFlow(sharedPreferenceManager.timeFormat)
    val selectedTimeFormatInDialog: StateFlow<String> = _selectedTimeFormatInDialog.asStateFlow()


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
        _currentDateFormat.value = sharedPreferenceManager.dateFormat
        _currentTimeFormat.value = sharedPreferenceManager.timeFormat
        _selectedDateFormatInDialog.value = sharedPreferenceManager.dateFormat
        _selectedTimeFormatInDialog.value = sharedPreferenceManager.timeFormat
    }

    fun onDateFormatSelectedInDialog(formatPattern: String) {
        _selectedDateFormatInDialog.value = formatPattern
    }

    fun onTimeFormatSelectedInDialog(formatPattern: String) {
        _selectedTimeFormatInDialog.value = formatPattern
    }

    fun applySelectedDateTimeFormats() {
        val newDateFormat = _selectedDateFormatInDialog.value
        val newTimeFormat = _selectedTimeFormatInDialog.value

        sharedPreferenceManager.dateFormat = newDateFormat
        sharedPreferenceManager.timeFormat = newTimeFormat

        _currentDateFormat.value = newDateFormat
        _currentTimeFormat.value = newTimeFormat

        _showDateTimeFormatDialog.value = false
    }

    fun dismissDateTimeFormatDialog() {
        _showDateTimeFormatDialog.value = false
        _selectedDateFormatInDialog.value = sharedPreferenceManager.dateFormat
        _selectedTimeFormatInDialog.value = sharedPreferenceManager.timeFormat
    }


    fun onThemeOptionSelectedInDialog(index: Int) {
        if (index >= 0 && index < themeOptions.size) {
            _dialogPreviewThemeIndex.value = index
            _persistedThemeIndexFlow.value = index
        }
    }

    fun applySelectedTheme() {
        val indexToApply = _dialogPreviewThemeIndex.value
        if (indexToApply >= 0 && indexToApply < themeOptions.size) {
            sharedPreferenceManager.theme = indexToApply
            _persistedThemeIndexFlow.value = indexToApply
        }
        _showThemeDialog.value = false
    }

    fun onThemeSettingClicked() {
        _dialogPreviewThemeIndex.value = _persistedThemeIndexFlow.value
        _showThemeDialog.value = true
    }

    fun onTimeFormatClicked() {
        _selectedDateFormatInDialog.value = sharedPreferenceManager.dateFormat
        _selectedTimeFormatInDialog.value = sharedPreferenceManager.timeFormat
        _showDateTimeFormatDialog.value = true
    }

    fun setBlackedOutEnabled(enabled: Boolean) {
        sharedPreferenceManager.blackedOutModeEnabled = enabled
        _blackedOutModeEnabled.value = enabled
    }

    fun dismissThemeDialog() {
        _showThemeDialog.value = false
        _dialogPreviewThemeIndex.value = sharedPreferenceManager.theme
        _persistedThemeIndexFlow.value = sharedPreferenceManager.theme
    }

    fun setCoverThemeEnabled(enabled: Boolean) {
        sharedPreferenceManager.coverThemeEnabled = enabled
        _enableCoverTheme.value = enabled
    }

    fun onCoverThemeClicked() {
        _showCoverSelectionDialog.value = true
    }

    fun dismissCoverThemeDialog() {
        _showCoverSelectionDialog.value = false
    }

    fun saveCoverDisplayMetrics(displaySize: IntSize) {
        sharedPreferenceManager.coverDisplaySize = displaySize
        _enableCoverTheme.value = true
        sharedPreferenceManager.coverThemeEnabled = true
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
                @Suppress("DEPRECATION")
                val success = activityManager.clearApplicationUserData()

                if (success) {
                    val defaultThemeIndex = themeOptions.indexOfFirst { it == ThemeSetting.SYSTEM }
                        .takeIf { it != -1 } ?: ThemeSetting.SYSTEM.ordinal
                    sharedPreferenceManager.theme = defaultThemeIndex
                    _persistedThemeIndexFlow.value = defaultThemeIndex
                    _dialogPreviewThemeIndex.value = defaultThemeIndex

                    sharedPreferenceManager.blackedOutModeEnabled = false
                    _blackedOutModeEnabled.value = false

                    sharedPreferenceManager.coverThemeEnabled = false
                    _enableCoverTheme.value = false


                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        setAppLocale("")
                    }
                    updateCurrentLanguage()
                    _currentDateFormat.value = sharedPreferenceManager.dateFormat
                    _currentTimeFormat.value = sharedPreferenceManager.timeFormat


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

    fun onResetSettingsClicked() {
        _showResetSettingsDialog.value = true
    }

    fun dismissResetSettingsDialog() {
        _showResetSettingsDialog.value = false
    }

    fun confirmResetSettings() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            sharedPreferenceManager.clearSettings()

            val defaultThemeIndex = ThemeSetting.SYSTEM.ordinal
            _persistedThemeIndexFlow.value = defaultThemeIndex
            _dialogPreviewThemeIndex.value = defaultThemeIndex

            _blackedOutModeEnabled.value = sharedPreferenceManager.blackedOutModeEnabled

            _enableCoverTheme.value = sharedPreferenceManager.coverThemeEnabled

            _currentDateFormat.value = sharedPreferenceManager.dateFormat
            _currentTimeFormat.value = sharedPreferenceManager.timeFormat
            _selectedDateFormatInDialog.value = sharedPreferenceManager.dateFormat
            _selectedTimeFormatInDialog.value = sharedPreferenceManager.timeFormat


            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                setAppLocale("")
            }
            updateCurrentLanguage()

            _showResetSettingsDialog.value = false

            delay(1000)
            restartApplication(context)
        }
    }

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
        val en = Locale("en")
        languages.add(LanguageOption(en.getDisplayName(en), en.toLanguageTag()))
        val de = Locale("de")
        languages.add(LanguageOption(de.getDisplayName(de), de.toLanguageTag()))
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
            } catch (_: Exception) {
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
        updateCurrentLanguage()
        viewModelScope.launch {
            delay(1000)
            restartApplication(context)
        }
    }

    private fun setAppLocale(localeTag: String) {
        val appLocale: LocaleListCompat = if (localeTag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(localeTag)
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun dismissLanguageDialog() {
        _showLanguageDialog.value = false
        _selectedLanguageTagInDialog.value = getAppLocaleTag()
    }

    fun openAppInfo(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open app info.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openImpressum(context: Context) {
        Toast.makeText(context, "Impressum: xenonware.com/impressum", Toast.LENGTH_LONG).show()
    }

    private fun restartApplication(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent?.component != null) {
            val mainIntent = Intent.makeRestartActivityTask(intent.component)
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
