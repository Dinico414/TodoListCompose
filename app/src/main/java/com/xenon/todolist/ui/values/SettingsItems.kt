package com.xenon.todolist.ui.values

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
// Required for default values if not passed down
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
// LaunchedEffect is already here
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp // Make sure dp is imported
import com.xenon.todolist.R
import com.xenon.todolist.ui.res.SettingsSwitchTile
import com.xenon.todolist.ui.res.SettingsTile
import com.xenon.todolist.viewmodel.SettingsViewModel

@Composable
fun SettingsItems(
    viewModel: SettingsViewModel,
    currentThemeTitle: String,
    applyCoverTheme: Boolean,
    coverThemeEnabled: Boolean,
    currentLanguage: String,
    appVersion: String,
    innerGroupRadius: Dp = SmallestCornerRadius,
    outerGroupRadius: Dp = MediumCornerRadius,
    innerGroupSpacing: Dp = SmallSpacing,
    outerGroupSpacing: Dp = LargerSpacing,
    tileBackgroundColor: Color = MaterialTheme.colorScheme.surfaceBright,
    tileContentColor: Color = MaterialTheme.colorScheme.onSurface,
    tileSubtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    tileShapeOverride: Shape? = null,
    tileHorizontalPadding: Dp = LargerPadding,
    tileVerticalPadding: Dp = ExtraLargePadding,
    switchColorsOverride: androidx.compose.material3.SwitchColors? = null,
    useGroupStyling: Boolean = true,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val actualInnerGroupRadius = if (useGroupStyling) innerGroupRadius else 0.dp
    val actualOuterGroupRadius = if (useGroupStyling) outerGroupRadius else 0.dp
    val actualInnerGroupSpacing = if (useGroupStyling) innerGroupSpacing else 0.dp
    val actualOuterGroupSpacing = if (useGroupStyling) outerGroupSpacing else MediumPadding

    val defaultSwitchColors = SwitchDefaults.colors()

    val topShape = if (useGroupStyling) RoundedCornerShape(
        bottomStart = actualInnerGroupRadius, bottomEnd = actualInnerGroupRadius,
        topStart = actualOuterGroupRadius, topEnd = actualOuterGroupRadius
    ) else RoundedCornerShape(NoCornerRadius)

    val bottomShape = if (useGroupStyling) RoundedCornerShape(
        topStart = actualInnerGroupRadius, topEnd = actualInnerGroupRadius,
        bottomStart = actualOuterGroupRadius, bottomEnd = actualOuterGroupRadius
    ) else RoundedCornerShape(NoCornerRadius)

    val standaloneShape = if (useGroupStyling) RoundedCornerShape(actualOuterGroupRadius)
    else RoundedCornerShape(NoCornerRadius)

    SettingsTile(
        title = stringResource(id = R.string.theme),
        subtitle = "${stringResource(id = R.string.current)} $currentThemeTitle",
        onClick = { viewModel.onThemeSettingClicked() },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.themes),
                contentDescription = stringResource(id = R.string.theme),
                tint = tileContentColor
            )
        },
        shape = tileShapeOverride ?: topShape,
        backgroundColor = tileBackgroundColor,
        contentColor = tileContentColor,
        subtitleColor = tileSubtitleColor,
        horizontalPadding = tileHorizontalPadding,
        verticalPadding = tileVerticalPadding
    )

    Spacer(modifier = Modifier.height(actualInnerGroupSpacing))

    SettingsSwitchTile(
        title = stringResource(id = R.string.cover_screen_mode),
        subtitle = "${stringResource(id = R.string.cover_screen_mode_description)} (${
            if (applyCoverTheme) stringResource(id = R.string.enabled)
            else stringResource(id = R.string.disabled)
        })",
        checked = coverThemeEnabled,
        onCheckedChange = { viewModel.setCoverThemeEnabled(it) },
        onClick = { viewModel.onCoverThemeClicked() },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.cover_screen),
                contentDescription = stringResource(id = R.string.cover_screen_mode),
                tint = tileContentColor
            )
        },
        shape = tileShapeOverride ?: bottomShape,
        backgroundColor = tileBackgroundColor,
        contentColor = tileContentColor,
        subtitleColor = tileSubtitleColor,
        horizontalPadding = tileHorizontalPadding,
        verticalPadding = tileVerticalPadding,
        switchColors = switchColorsOverride ?: defaultSwitchColors
    )

    Spacer(modifier = Modifier.height(actualOuterGroupSpacing))

    SettingsTile(
        title = stringResource(id = R.string.language),
        subtitle = "${stringResource(id = R.string.current)} $currentLanguage",
        onClick = { viewModel.onLanguageSettingClicked(context) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.language),
                contentDescription = stringResource(id = R.string.language),
                tint = tileContentColor
            )
        },
        shape = tileShapeOverride ?: standaloneShape,
        backgroundColor = tileBackgroundColor,
        contentColor = tileContentColor,
        subtitleColor = tileSubtitleColor,
        horizontalPadding = tileHorizontalPadding,
        verticalPadding = tileVerticalPadding
    )

    LaunchedEffect(Unit) {
        viewModel.updateCurrentLanguage()
    }

    Spacer(modifier = Modifier.height(actualOuterGroupSpacing))

    SettingsTile(
        title = stringResource(id = R.string.clear_data),
        subtitle = stringResource(id = R.string.clear_data_description),
        onClick = {
            viewModel.onClearDataClicked()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.reset),
                contentDescription = stringResource(id = R.string.clear_data),
                tint = tileContentColor
            )
        },
        shape = tileShapeOverride ?: topShape,
        backgroundColor = tileBackgroundColor,
        contentColor = tileContentColor,
        subtitleColor = tileSubtitleColor,
        horizontalPadding = tileHorizontalPadding,
        verticalPadding = tileVerticalPadding
    )

    Spacer(modifier = Modifier.height(actualInnerGroupSpacing))

    SettingsTile(
        title = stringResource(id = R.string.version),
        subtitle = "v $appVersion",
        onClick = { viewModel.openAppInfo(context) },
        onLongClick = { viewModel.openImpressum(context) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.info),
                contentDescription = stringResource(id = R.string.version),
                tint = tileContentColor
            )
        },
        shape = tileShapeOverride ?: bottomShape,
        backgroundColor = tileBackgroundColor,
        contentColor = tileContentColor,
        subtitleColor = tileSubtitleColor,
        horizontalPadding = tileHorizontalPadding,
        verticalPadding = tileVerticalPadding
    )
}
