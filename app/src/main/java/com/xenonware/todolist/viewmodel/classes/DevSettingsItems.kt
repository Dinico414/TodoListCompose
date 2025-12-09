package com.xenonware.todolist.viewmodel.classes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xenon.mylibrary.values.ExtraLargeSpacing
import com.xenon.mylibrary.values.LargerPadding
import com.xenon.mylibrary.values.MediumCornerRadius
import com.xenon.mylibrary.values.NoCornerRadius
import com.xenon.mylibrary.values.SmallSpacing
import com.xenon.mylibrary.values.SmallestCornerRadius
import com.xenonware.todolist.R
import com.xenonware.todolist.ui.res.SettingsSwitchTile
import com.xenonware.todolist.viewmodel.DevSettingsViewModel
import com.xenonware.todolist.viewmodel.SettingsViewModel

@Composable
fun DevSettingsItems(
    settingsViewModel: SettingsViewModel,
    viewModel: DevSettingsViewModel,
    modifier: Modifier = Modifier,
    innerGroupRadius: Dp = SmallestCornerRadius,
    outerGroupRadius: Dp = MediumCornerRadius,
    innerGroupSpacing: Dp = SmallSpacing,
    outerGroupSpacing: Dp = ExtraLargeSpacing,
    tileBackgroundColor: Color = MaterialTheme.colorScheme.surfaceBright,
    tileContentColor: Color = MaterialTheme.colorScheme.onSurface,
    tileSubtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    tileShapeOverride: Shape? = null,
    tileHorizontalPadding: Dp = LargerPadding,
    tileVerticalPadding: Dp = LargerPadding,
    useGroupStyling: Boolean = true,

    ) {
    val isDeveloperModeEnabled by viewModel.devModeToggleState.collectAsState()
    val isShowDummyProfileEnabled by viewModel.showDummyProfileState.collectAsState()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val blackedOutEnabled by settingsViewModel.blackedOutModeEnabled.collectAsState()
    val developerModeEnabled by settingsViewModel.developerModeEnabled.collectAsState()

    val actualInnerGroupRadius = if (useGroupStyling) innerGroupRadius else 0.dp
    val actualOuterGroupRadius = if (useGroupStyling) outerGroupRadius else 0.dp
    val actualInnerGroupSpacing = if (useGroupStyling) innerGroupSpacing else 0.dp
    val actualOuterGroupSpacing = outerGroupSpacing // outerGroupSpacing is used directly

    val defaultSwitchColors = SwitchDefaults.colors()

    val topShape = if (useGroupStyling) RoundedCornerShape(
        bottomStart = actualInnerGroupRadius,
        bottomEnd = actualInnerGroupRadius,
        topStart = actualOuterGroupRadius,
        topEnd = actualOuterGroupRadius
    ) else RoundedCornerShape(NoCornerRadius)

    val middleShape = if (useGroupStyling) RoundedCornerShape(
        topStart = actualInnerGroupRadius,
        topEnd = actualInnerGroupRadius,
        bottomStart = actualInnerGroupRadius,
        bottomEnd = actualInnerGroupRadius
    ) else RoundedCornerShape(NoCornerRadius)

    val bottomShape = if (useGroupStyling) RoundedCornerShape(
        topStart = actualInnerGroupRadius,
        topEnd = actualInnerGroupRadius,
        bottomStart = actualOuterGroupRadius,
        bottomEnd = actualOuterGroupRadius
    ) else RoundedCornerShape(NoCornerRadius)

    val standaloneShape = if (useGroupStyling) RoundedCornerShape(actualOuterGroupRadius)
    else RoundedCornerShape(NoCornerRadius)

    Column(
        modifier = modifier
            .padding(LargerPadding)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.dev_settings_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = LargerPadding).align(alignment = Alignment.CenterHorizontally)
        )

        SettingsSwitchTile(
            title = stringResource(id = R.string.developer_options_title),
            subtitle = "",
            checked = isDeveloperModeEnabled,
            onCheckedChange = { newCheckedState ->
                viewModel.setDeveloperModeEnabled(newCheckedState)
            },
            onClick = {
                val newCheckedState = !isDeveloperModeEnabled
                viewModel.setDeveloperModeEnabled(newCheckedState)
            },
            shape = tileShapeOverride ?: topShape,
            backgroundColor = tileBackgroundColor,
            contentColor = tileContentColor,
            subtitleColor = tileSubtitleColor,
            horizontalPadding = tileHorizontalPadding,
            verticalPadding = tileVerticalPadding
        )

        if (isDeveloperModeEnabled) {
            Spacer(modifier = Modifier.Companion.height(SmallSpacing))

            SettingsSwitchTile(
                title = stringResource(id = R.string.show_dummy_profile_title),
                subtitle = "",
                checked = isShowDummyProfileEnabled,
                onCheckedChange = { newCheckedState ->
                    viewModel.setShowDummyProfileEnabled(newCheckedState)
                },
                onClick = {
                    val newCheckedState = !isShowDummyProfileEnabled
                    viewModel.setShowDummyProfileEnabled(newCheckedState)
                },
                shape = tileShapeOverride ?: bottomShape,
                backgroundColor = tileBackgroundColor,
                contentColor = tileContentColor,
                subtitleColor = tileSubtitleColor,
                horizontalPadding = tileHorizontalPadding,
                verticalPadding = tileVerticalPadding
            )
        }
    }
}
