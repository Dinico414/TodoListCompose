package com.xenon.todolist.ui.layouts.settings

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.res.DialogClearDataConfirmation
import com.xenon.todolist.ui.res.DialogCoverDisplaySelection
import com.xenon.todolist.ui.res.DialogLanguageSelection
import com.xenon.todolist.ui.res.DialogThemeSelection
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.ui.values.NoCornerRadius
import com.xenon.todolist.ui.values.SettingsItems
import com.xenon.todolist.viewmodel.SettingsViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverSettings(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val context = LocalContext.current

    val currentThemeTitle by viewModel.currentThemeTitle.collectAsState()
    val showThemeDialog by viewModel.showThemeDialog.collectAsState()
    val themeOptions = viewModel.themeOptions
    val dialogSelectedThemeIndex by viewModel.dialogPreviewThemeIndex.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val showClearDataDialog by viewModel.showClearDataDialog.collectAsState()
    val showCoverSelectionDialog by viewModel.showCoverSelectionDialog.collectAsState()
    val coverThemeEnabled by viewModel.enableCoverTheme.collectAsState()

    val showLanguageDialog by viewModel.showLanguageDialog.collectAsState()
    val availableLanguages by viewModel.availableLanguages.collectAsState()
    val selectedLanguageTagInDialog by viewModel.selectedLanguageTagInDialog.collectAsState()

    val packageManager = context.packageManager
    val packageName = context.packageName
    val packageInfo = remember {
        try {
            packageManager.getPackageInfo(packageName, 0)
        } catch (_: Exception) {
            null
        }
    }
    val appVersion = packageInfo?.versionName ?: "N/A"

    val containerSize = LocalWindowInfo.current.containerSize
    val applyCoverThemeActual = remember(containerSize, coverThemeEnabled) {
        viewModel.applyCoverTheme(containerSize) && coverThemeEnabled
    }

    val hazeState = rememberHazeState()

    val coverScreenBackgroundColor = Color.Black
    val coverScreenContentColor = Color.White

    ActivityScreen(
        title = { fontWeight, _, _ ->
            Text(
                stringResource(id = R.string.settings), fontWeight = fontWeight
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_description)
                )
            }
        },
        appBarActions = {},
        // isAppBarCollapsible = false,
        screenBackgroundColor = Color.Black,
        appBarContainerColor = Color.Black,
        contentBackgroundColor = Color.Black,
        contentCornerRadius = NoCornerRadius,
        modifier = Modifier.hazeSource(hazeState),
        content = { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        bottom = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateBottomPadding() + MediumPadding,
                        top = MediumPadding
                    )
            ) {
                SettingsItems(
                    viewModel = viewModel,
                    currentThemeTitle = currentThemeTitle,
                    applyCoverTheme = applyCoverThemeActual,
                    coverThemeEnabled = coverThemeEnabled,
                    currentLanguage = currentLanguage,
                    appVersion = appVersion,

                    tileBackgroundColor = coverScreenBackgroundColor,
                    tileContentColor = coverScreenContentColor,
                    tileSubtitleColor = coverScreenContentColor.copy(alpha = 0.7f),
                    tileShapeOverride = RoundedCornerShape(NoCornerRadius),
                    tileHorizontalPadding = MediumPadding,
                    tileVerticalPadding = MediumPadding,
                    switchColorsOverride = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.DarkGray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f),
                        checkedBorderColor = MaterialTheme.colorScheme.primary,
                        uncheckedBorderColor = Color.Gray
                    ),
                    useGroupStyling = false
                )
            }
        })

    if (showThemeDialog) {
        Box(modifier = Modifier.fillMaxSize().hazeEffect(hazeState)) {
            DialogThemeSelection(
                themeOptions = themeOptions,
                currentThemeIndex = dialogSelectedThemeIndex,
                onThemeSelected = { index -> viewModel.onThemeOptionSelectedInDialog(index) },
                onDismiss = { viewModel.dismissThemeDialog() },
                onConfirm = { viewModel.applySelectedTheme() }
            )
        }
    }
    if (showCoverSelectionDialog) {
        Box(modifier = Modifier.fillMaxSize().hazeEffect(hazeState)) {
            DialogCoverDisplaySelection(
                onConfirm = { viewModel.saveCoverDisplayMetrics(containerSize) },
                onDismiss = { viewModel.dismissCoverThemeDialog() }
            )
        }
    }
    if (showClearDataDialog) {
        Box(modifier = Modifier.fillMaxSize().hazeEffect(hazeState)) {
            DialogClearDataConfirmation(
                onConfirm = { viewModel.confirmClearData() },
                onDismiss = { viewModel.dismissClearDataDialog() }
            )
        }
    }
    if (showLanguageDialog && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Box(modifier = Modifier.fillMaxSize().hazeEffect(hazeState)) {
            DialogLanguageSelection(
                availableLanguages = availableLanguages,
                currentLanguageTag = selectedLanguageTagInDialog,
                onLanguageSelected = { tag -> viewModel.onLanguageSelectedInDialog(tag) },
                onDismiss = { viewModel.dismissLanguageDialog() },
                onConfirm = { viewModel.applySelectedLanguage() }
            )
        }
    }
}
