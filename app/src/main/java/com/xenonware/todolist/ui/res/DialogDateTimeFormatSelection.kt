@file:Suppress("AssignedValueIsNeverRead")

package com.xenonware.todolist.ui.res

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.xenon.mylibrary.res.XenonDialog
import com.xenon.mylibrary.values.LargerPadding
import com.xenon.mylibrary.values.LargestPadding
import com.xenonware.todolist.R
import com.xenonware.todolist.viewmodel.FormatOption

data class TimeFormatButtonOption(
    val label: String,
    val pattern: String,
    val weight: Float = 1f,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DialogDateTimeFormatSelection(
    availableDateFormats: List<FormatOption>,
    currentDateFormatPattern: String,
    currentTimeFormatPattern: String,
    onDateFormatSelected: (String) -> Unit,
    onTimeFormatSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    systemTimePattern: String,
    twentyFourHourTimePattern: String,
    twelveHourTimePattern: String,
) {

    // ── Date selection ────────────────────────────────────────────────────────
    var selectedDatePatternInDialog by remember(currentDateFormatPattern) {
        mutableStateOf(currentDateFormatPattern.ifBlank { "" })
    }

    var userHasManuallySelectedDate by remember { mutableStateOf(false) }

    LaunchedEffect(currentDateFormatPattern) {
        if (!userHasManuallySelectedDate && currentDateFormatPattern.isBlank()) {
            selectedDatePatternInDialog = ""
        }
    }

    // ── Time selection (your existing logic, kept unchanged) ──────────────────
    var selectedTimePatternInDialog by remember(currentTimeFormatPattern) {
        mutableStateOf(currentTimeFormatPattern.ifBlank { systemTimePattern })
    }

    var userHasManuallySelectedTime by remember { mutableStateOf(false) }

    LaunchedEffect(systemTimePattern) {
        if (!userHasManuallySelectedTime && currentTimeFormatPattern.isBlank()) {
            selectedTimePatternInDialog = systemTimePattern
        }
    }

    val timeFormatButtonOptions = remember(twentyFourHourTimePattern, twelveHourTimePattern) {
        listOf(
            TimeFormatButtonOption(label = "12h", pattern = twelveHourTimePattern),
            TimeFormatButtonOption(label = "24h", pattern = twentyFourHourTimePattern)
        )
    }

    val currentlySelectedOption =
        timeFormatButtonOptions.first { it.pattern == selectedTimePatternInDialog }

    // ── Dialog UI ─────────────────────────────────────────────────────────────
    XenonDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.date_time_format),
        confirmButtonText = stringResource(R.string.ok),
        onConfirmButtonClick = {
            onDateFormatSelected(selectedDatePatternInDialog)
            onTimeFormatSelected(selectedTimePatternInDialog)
            onConfirm()
        },
        properties = DialogProperties(usePlatformDefaultWidth = true),
        contentManagesScrolling = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Time section
            Column {
                Text(
                    text = stringResource(R.string.time_format),
                    style = typography.labelLarge.copy(color = colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                XenonSingleChoiceButtonGroup(
                    options = timeFormatButtonOptions,
                    selectedOption = currentlySelectedOption,
                    onOptionSelect = { option ->
                        selectedTimePatternInDialog = option.pattern
                        userHasManuallySelectedTime = true
                    },
                    label = { option -> option.label },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = colorScheme.surfaceContainerLow,
                        checkedContainerColor = colorScheme.primary,
                        contentColor = colorScheme.onSurface,
                        checkedContentColor = colorScheme.onPrimary
                    ),
                )
            }

            Spacer(modifier = Modifier.height(LargestPadding))

            // Date section
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.date_format),
                        style = typography.labelLarge.copy(color = colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .selectableGroup()
                            .fillMaxWidth(),
                    ) {
                        availableDateFormats.forEach { formatOption ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(100.0f))
                                    .selectable(
                                        selected = selectedDatePatternInDialog == formatOption.pattern,
                                        onClick = {
                                            selectedDatePatternInDialog = formatOption.pattern
                                            userHasManuallySelectedDate = true
                                        },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDatePatternInDialog == formatOption.pattern,
                                    onClick = {
                                        selectedDatePatternInDialog = formatOption.pattern
                                        userHasManuallySelectedDate = true
                                    }
                                )
                                Spacer(
                                    modifier = Modifier.height(24.dp)
                                )
                                Text(
                                    text = formatOption.displayName,
                                    style = typography.bodyMedium,
                                    modifier = Modifier.padding(start = LargerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}