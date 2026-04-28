package com.xenonware.todolist.ui.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntSize
import com.xenonware.todolist.ui.layouts.dev_settings.DevCoverSettings
import com.xenonware.todolist.ui.layouts.dev_settings.DevDefaultSettings
import com.xenonware.todolist.viewmodel.DevSettingsViewModel
import com.xenonware.todolist.viewmodel.LayoutType
import com.xenonware.todolist.viewmodel.SettingsViewModel

@Composable
fun DevSettingsLayout(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    viewModel: DevSettingsViewModel,
    isLandscape: Boolean,
    layoutType: LayoutType,
    appSize: IntSize,
) {
    when (layoutType) {
        LayoutType.COVER -> {
            DevCoverSettings(
                onNavigateBack = onNavigateBack,
                viewModel = viewModel
            )
        }

        LayoutType.SMALL, LayoutType.COMPACT, LayoutType.MEDIUM, LayoutType.EXPANDED -> {
            DevDefaultSettings(
                settingsViewModel = settingsViewModel,
                onNavigateBack = onNavigateBack,
                viewModel = viewModel,
                isLandscape = isLandscape,
                layoutType = layoutType,
                appSize = appSize,
            )
        }
    }
}

