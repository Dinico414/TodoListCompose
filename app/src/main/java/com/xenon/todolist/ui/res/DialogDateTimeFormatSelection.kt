package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargestPadding
import com.xenon.todolist.viewmodel.FormatOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var selectedDatePatternInDialog by remember(currentDateFormatPattern) { mutableStateOf(currentDateFormatPattern) }
    var selectedTimePatternInDialog by remember(currentTimeFormatPattern) { mutableStateOf(currentTimeFormatPattern) }

    val currentPreview: String = remember(selectedDatePatternInDialog, selectedTimePatternInDialog) {
        try {
            val now = Date()
            val sdfDate = SimpleDateFormat(selectedDatePatternInDialog, Locale.getDefault())
            val sdfTime = SimpleDateFormat(selectedTimePatternInDialog, Locale.getDefault())
            "${sdfDate.format(now)}  ${sdfTime.format(now)}"
        } catch (_: Exception) {
            "Invalid Format"
        }
    }

    val timeFormatButtonOptions = remember(systemTimePattern, twentyFourHourTimePattern, twelveHourTimePattern) {
        listOf(
            TimeFormatButtonOption(label = "System", pattern = systemTimePattern, weight = 1f),
            TimeFormatButtonOption(label = "24h", pattern = twentyFourHourTimePattern, weight = 0.75f),
            TimeFormatButtonOption(label = "12h", pattern = twelveHourTimePattern, weight = 0.75f)
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
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.current) + " " + currentPreview,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )

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
                            FormatRow(
                                text = formatOption.displayName,
                                selected = selectedDatePatternInDialog == formatOption.pattern,
                                onClick = {
                                    selectedDatePatternInDialog = formatOption.pattern
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(LargestPadding))

                Column {
                    Text(
                        text = stringResource(R.string.time_format),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        timeFormatButtonOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                modifier = Modifier.weight(option.weight),
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = timeFormatButtonOptions.size
                                ),
                                onClick = { selectedTimePatternInDialog = option.pattern },
                                selected = selectedTimePatternInDialog == option.pattern
                            ) {
                                Text(option.label)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
