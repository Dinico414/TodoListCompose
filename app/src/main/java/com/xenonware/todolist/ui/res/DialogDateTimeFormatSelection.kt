package com.xenonware.todolist.ui.res

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val weight: Float = 1f
)

@OptIn(ExperimentalMaterial3Api::class)
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
    twelveHourTimePattern: String
) {
    var selectedDatePatternInDialog by remember(currentDateFormatPattern) {
        mutableStateOf(
            currentDateFormatPattern
        )
    }
    var selectedTimePatternInDialog by remember(currentTimeFormatPattern) {
        mutableStateOf(
            currentTimeFormatPattern
        )
    }

    val timeFormatButtonOptions =
        remember(systemTimePattern, twentyFourHourTimePattern, twelveHourTimePattern) {
            listOf(
                TimeFormatButtonOption(label = "System", pattern = systemTimePattern, weight = 1f),
                TimeFormatButtonOption(
                    label = "24h", pattern = twentyFourHourTimePattern, weight = 0.75f
                ),
                TimeFormatButtonOption(
                    label = "12h", pattern = twelveHourTimePattern, weight = 0.75f
                )
            )
        }


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
        contentManagesScrolling = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column {
                Text(
                    text = stringResource(R.string.time_format),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    timeFormatButtonOptions.forEachIndexed { index, option ->
                        SegmentedButton(
                            modifier = Modifier.weight(option.weight),
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index, count = timeFormatButtonOptions.size
                            ),
                            onClick = { selectedTimePatternInDialog = option.pattern },
                            selected = selectedTimePatternInDialog == option.pattern
                        ) {
                            Text(option.label)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.Companion.height(LargestPadding))

            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.date_format),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .height(150.dp)
                            .selectableGroup()
                            .fillMaxWidth()
                    ) {
                        items(availableDateFormats) { formatOption ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(100.0f))
                                    .selectable(
                                        selected = selectedDatePatternInDialog == formatOption.pattern,
                                        onClick = {
                                            selectedDatePatternInDialog = formatOption.pattern
                                        },
                                        role = Role.RadioButton
                                    ), verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDatePatternInDialog == formatOption.pattern,
                                    onClick = {
                                        selectedDatePatternInDialog = formatOption.pattern
                                    })
                                Text(
                                    text = formatOption.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
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