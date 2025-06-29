package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.viewmodel.classes.Priority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemContent(
    textState: String,
    onTextChange: (String) -> Unit,
    descriptionState: String,
    onDescriptionChange: (String) -> Unit,
    currentPriority: Priority,
    onPriorityChange: (Priority) -> Unit,
    initialDueDateMillis: Long?,
    initialDueTimeHour: Int?,
    initialDueTimeMinute: Int?,
    onSaveTask: (selectedDateMillis: Long?, selectedHour: Int?, selectedMinute: Int?) -> Unit,
    isSaveEnabled: Boolean,
) {
    var showMoreOptions by remember { mutableStateOf(false) }
    val priorityOptions = Priority.entries.toTypedArray()

    var selectedDateMillis by remember { mutableStateOf(initialDueDateMillis) }
    var selectedHour by remember { mutableStateOf(initialDueTimeHour) }
    var selectedMinute by remember { mutableStateOf(initialDueTimeMinute) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(initialDueDateMillis, initialDueTimeHour, initialDueTimeMinute) {
        selectedDateMillis = initialDueDateMillis
        selectedHour = initialDueTimeHour
        selectedMinute = initialDueTimeMinute
    }


    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePickerDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePickerDialog) {
        val initialDialogHour = selectedHour ?: calendar.get(Calendar.HOUR_OF_DAY)
        val initialDialogMinute = selectedMinute ?: calendar.get(Calendar.MINUTE)
        val timePickerState = rememberTimePickerState(
            initialHour = initialDialogHour,
            initialMinute = initialDialogMinute,
            is24Hour = true
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = { Text(text = stringResource(R.string.select_time)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePickerDialog = false
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePickerDialog = false }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(LargePadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        XenonTextField(
            value = textState,
            onValueChange = onTextChange,
            label = stringResource(R.string.new_task_label),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(MediumPadding))

        XenonTextField(
            value = descriptionState,
            onValueChange = onDescriptionChange,
            label = stringResource(R.string.task_description_label),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = false,
            maxLines = 5
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { showMoreOptions = !showMoreOptions },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (showMoreOptions) stringResource(R.string.less_options)
                        else stringResource(R.string.more_options)
                    )
                    Icon(
                        imageVector = if (showMoreOptions) Icons.Filled.ArrowDropUp
                        else Icons.Filled.ArrowDropDown,
                        contentDescription = if (showMoreOptions) stringResource(R.string.less_options)
                        else stringResource(R.string.more_options)
                    )
                }
            }
        }

        if (showMoreOptions) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.priority_label),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = MediumPadding / 2)
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorityOptions.forEachIndexed { index, priority ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = priorityOptions.size
                            ),
                            onClick = { onPriorityChange(priority) },
                            selected = currentPriority == priority,
                            icon = {}
                        ) {
                            Text(
                                text = when (priority) {
                                    Priority.LOW -> stringResource(R.string.priority_low)
                                    Priority.HIGH -> stringResource(R.string.priority_high)
                                    Priority.HIGHEST -> stringResource(R.string.priority_highest)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { showTimePickerDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                val timeText = if (selectedHour != null && selectedMinute != null) {
                    calendar.apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour!!)
                        set(Calendar.MINUTE, selectedMinute!!)
                    }
                    timeFormatter.format(calendar.time)
                } else {
                    stringResource(R.string.select_time)
                }
                Text(timeText)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onSaveTask(selectedDateMillis, selectedHour, selectedMinute)
                },
                enabled = isSaveEnabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.save))
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = { showDatePickerDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                val dateText = selectedDateMillis?.let {
                    dateFormatter.format(it)
                } ?: stringResource(R.string.select_date)
                Text(dateText)
            }
        }
        Spacer(modifier = Modifier.height(LargePadding))
    }
}