package com.xenon.todolist.ui.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xenon.todolist.ui.layouts.dev_settings.DevCoverSettings
import com.xenon.todolist.ui.layouts.dev_settings.DevDefaultSettings
import com.xenon.todolist.viewmodel.DevSettingsViewModel
import com.xenon.todolist.viewmodel.LayoutType

@Composable
fun DevSettingsLayout(
    onNavigateBack: () -> Unit,
    viewModel: DevSettingsViewModel,
    isLandscape: Boolean,
    layoutType: LayoutType,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                    layoutType = layoutType,
                )
            }
        }
    }
}
