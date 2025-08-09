package com.xenon.todolist.ui.layouts.dev_settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.values.LargestPadding
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.ui.values.NoSpacing
import com.xenon.todolist.viewmodel.DevSettingsViewModel
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.classes.DevSettingsItems
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun DevDefaultSettings(
    onNavigateBack: () -> Unit,
    viewModel: DevSettingsViewModel,
    layoutType: LayoutType,
    isLandscape: Boolean,
) {
    val hazeState = rememberHazeState()

    ActivityScreen(
        titleText = stringResource(id = R.string.developer_options_title),
        navigationIconStartPadding = MediumPadding,
        navigationIconPadding = MediumPadding,
        navigationIconSpacing = NoSpacing,
        navigationIconContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back_description),
                modifier = Modifier.size(24.dp)
            )
        },
        onNavigationIconClick = onNavigateBack,
        hasNavigationIconExtraContent = false,
        appBarActions = {},
        modifier = Modifier.hazeSource(hazeState),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = LargestPadding,
                        end = LargestPadding,
                        top = LargestPadding,
                        bottom = WindowInsets.safeDrawing
                            .asPaddingValues()
                            .calculateBottomPadding() + LargestPadding
                    )
            ) {
                DevSettingsItems(
                    viewModel = viewModel
                )
            }
        }
    )
}
