package com.xenonware.todolist.ui.layouts

import androidx.compose.runtime.Composable
import com.xenonware.todolist.ui.layouts.dev_settings.DevCoverSettings
import com.xenonware.todolist.ui.layouts.dev_settings.DevDefaultSettings
import com.xenonware.todolist.viewmodel.DevSettingsViewModel
import com.xenonware.todolist.viewmodel.LayoutType

@Composable
fun DevSettingsLayout(
    onNavigateBack: () -> Unit,
    viewModel: DevSettingsViewModel,
    isLandscape: Boolean,
    layoutType: LayoutType,
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
                onNavigateBack = onNavigateBack,
                viewModel = viewModel,
                isLandscape = isLandscape,
                layoutType = layoutType,
            )
        }
    }
}

