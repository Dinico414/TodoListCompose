package com.xenon.todolist.viewmodel.classes // Or wherever your DevSettingsItems is

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.xenon.todolist.R
import com.xenon.todolist.viewmodel.DevSettingsViewModel
import com.xenon.todolist.ui.res.SettingsSwitchTile
import com.xenon.todolist.ui.res.SettingsTile
import com.xenon.todolist.ui.values.LargerPadding
import com.xenon.todolist.ui.values.SmallSpacing

@Composable
fun DevSettingsItems(
    viewModel: DevSettingsViewModel,
    modifier: Modifier = Modifier
) {
    val isDeveloperModeEnabled by viewModel.devModeToggleState.collectAsState()

    Column(
        modifier = modifier
            .padding(LargerPadding)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.dev_settings_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = LargerPadding)
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
            }
        )

        if (isDeveloperModeEnabled) {
            Spacer(modifier = Modifier.height(SmallSpacing))

            SettingsTile(
                title = stringResource(id = R.string.placeholder),
                subtitle = stringResource(id = R.string.placeholder),
                onClick = { viewModel.triggerExampleDevAction() }
            )
        }
    }
}
   