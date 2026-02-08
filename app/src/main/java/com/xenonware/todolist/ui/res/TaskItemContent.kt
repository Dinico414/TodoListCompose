@file:Suppress("unused")

package com.xenonware.todolist.ui.res


import android.annotation.SuppressLint
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xenon.mylibrary.theme.QuicksandTitleVariable
import com.xenon.mylibrary.res.XenonTextField
import com.xenon.mylibrary.values.DialogPadding
import com.xenon.mylibrary.values.LargerSpacing
import com.xenon.mylibrary.values.LargestPadding
import com.xenon.mylibrary.values.MediumPadding
import com.xenon.mylibrary.values.MediumSpacing
import com.xenonware.todolist.R
import com.xenonware.todolist.viewmodel.classes.Priority
import com.xenonware.todolist.viewmodel.classes.TaskStep
import java.util.Calendar
import java.util.Locale
import java.text.DateFormat as JavaDateFormat

@SuppressLint("ConfigurationScreenWidthHeight")
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
    currentSteps: List<TaskStep>,
    onStepAdded: (stepText: String) -> Unit,
    onStepToggled: (stepId: String) -> Unit,
    onStepTextUpdated: (stepId: String, newText: String) -> Unit,
    onStepRemoved: (stepId: String) -> Unit,
    onSaveTask: (selectedDateMillis: Long?, selectedHour: Int?, selectedMinute: Int?) -> Unit,
    isSaveEnabled: Boolean,
    modifier: Modifier = Modifier,
    horizontalContentPadding: Dp = DialogPadding,
    bottomContentPadding: Dp = 0.dp,
    buttonRowHorizontalPadding: Dp = MediumPadding,
) {
    var showMoreOptions by remember { mutableStateOf(false) }
    val priorityOptions = Priority.entries.toTypedArray()

    var selectedDateMillis by remember { mutableStateOf(initialDueDateMillis) }
    var selectedHour by remember { mutableStateOf(initialDueTimeHour) }
    var selectedMinute by remember { mutableStateOf(initialDueTimeMinute) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    var newStepText by remember { mutableStateOf("") }

    val localSteps = remember { mutableStateListOf<TaskStep>() }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val dateFormatter =
        remember { JavaDateFormat.getDateInstance(JavaDateFormat.SHORT, Locale.getDefault()) }
    val timeFormatter =
        remember { JavaDateFormat.getTimeInstance(JavaDateFormat.SHORT, Locale.getDefault()) }
    val is24HourFormat = DateFormat.is24HourFormat(context)

    LaunchedEffect(initialDueDateMillis, initialDueTimeHour, initialDueTimeMinute, currentSteps) {
        selectedDateMillis = initialDueDateMillis
        selectedHour = initialDueTimeHour
        selectedMinute = initialDueTimeMinute
        if (localSteps.toList() != currentSteps) {
            localSteps.clear()
            localSteps.addAll(currentSteps)
        }
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
            is24Hour = is24HourFormat
        )

        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = { Text(stringResource(R.string.select_time_title)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePickerDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }




    /*
    // Commented out XenonDialogPicker for DatePicker
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        XenonDialogPicker(
            onDismissRequest = { showDatePickerDialog = false },
            title = stringResource(R.string.select_date_title),
            confirmButtonText = stringResource(android.R.string.ok),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onConfirmButtonClick = {
                selectedDateMillis = datePickerState.selectedDateMillis
                showDatePickerDialog = false
            },
            contentManagesScrolling = true
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Commented out XenonDialogPicker for TimePicker
    if (showTimePickerDialog) {
        val initialDialogHour = selectedHour ?: calendar.get(Calendar.HOUR_OF_DAY)
        val initialDialogMinute = selectedMinute ?: calendar.get(Calendar.MINUTE)
        val timePickerState = rememberTimePickerState(
            initialHour = initialDialogHour,
            initialMinute = initialDialogMinute,
            is24Hour = is24HourFormat
        )
        XenonDialogPicker(
            onDismissRequest = { showTimePickerDialog = false },
            title = stringResource(R.string.select_time_title),
            confirmButtonText = stringResource(android.R.string.ok),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onConfirmButtonClick = {
                selectedHour = timePickerState.hour
                selectedMinute = timePickerState.minute
                showTimePickerDialog = false
            },
            contentManagesScrolling = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
            }
        }
    }
    */


    val scrollState = rememberScrollState()
    val topDividerAlpha by remember {
        derivedStateOf { if (scrollState.value > 0) 1f else 0f }
    }
    val bottomDividerAlpha by remember {
        derivedStateOf { if (scrollState.canScrollForward && scrollState.maxValue > 0) 1f else 0f }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val maxHeightForScrollableContent = screenHeight * 0.9f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            modifier = Modifier
                .alpha(topDividerAlpha)
                .padding(horizontal = horizontalContentPadding)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .heightIn(max = maxHeightForScrollableContent)
                .padding(horizontal = horizontalContentPadding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            XenonTextField(
                value = textState,
                onValueChange = onTextChange,
                placeholder = { Text(stringResource(R.string.task_name)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(LargerSpacing))

            XenonTextField(
                value = descriptionState,
                onValueChange = onDescriptionChange,
                placeholder = { Text(stringResource(R.string.task_description_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = false,
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(LargerSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
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
                        modifier = Modifier.padding(bottom = MediumSpacing, start = MediumSpacing),
                        fontWeight = FontWeight.Thin,
                        fontFamily = QuicksandTitleVariable,
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        priorityOptions.forEachIndexed { index, priority ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index, count = priorityOptions.size
                                ),
                                onClick = { onPriorityChange(priority) },
                                selected = currentPriority == priority,
                                icon = {}) {
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
                    Spacer(modifier = Modifier.height(LargerSpacing))

                    Text(
                        text = stringResource(R.string.steps),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = MediumSpacing, start = MediumSpacing),
                        fontWeight = FontWeight.Thin,
                        fontFamily = QuicksandTitleVariable,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        XenonTextField(
                            value = newStepText,
                            onValueChange = { newStepText = it },
                            placeholder = { Text(stringResource(R.string.add_new_step)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        Spacer(modifier = Modifier.width(MediumPadding))

                        FilledIconButton(
                            onClick = {
                                if (newStepText.isNotBlank()) {
                                    onStepAdded(newStepText)
                                    newStepText = ""
                                }
                            },
                            modifier = Modifier
                                .height(56.dp)
                                .width(40.dp),
                            enabled = newStepText.isNotBlank(),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.add_new_step)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(MediumSpacing))

                    if (localSteps.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(items = localSteps, key = { it.id }) { step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = MediumPadding / 2),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = step.isCompleted,
                                        onCheckedChange = { onStepToggled(step.id) })
                                    Text(
                                        text = step.text,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = MediumPadding),
                                        style = if (step.isCompleted) {
                                            MaterialTheme.typography.bodyMedium.copy(
                                                textDecoration = TextDecoration.LineThrough,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.6f
                                                )
                                            )
                                        } else {
                                            MaterialTheme.typography.bodyMedium
                                        },
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = { onStepRemoved(step.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = stringResource(R.string.remove_step)
                                        )
                                    }

                                }
                                if (localSteps.lastOrNull()?.id != step.id) {
                                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                                }
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.no_steps),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            modifier = Modifier
                                .padding(vertical = MediumPadding)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(LargerSpacing))
            }
            Spacer(modifier = Modifier.height(1.dp))
        }

        HorizontalDivider(
            modifier = Modifier
                .alpha(bottomDividerAlpha)
                .padding(horizontal = horizontalContentPadding)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = buttonRowHorizontalPadding)
                .padding(top = LargestPadding, bottom = bottomContentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(
                onClick = { showTimePickerDialog = true }, modifier = Modifier.weight(1.2f)
            ) {
                val timeText = if (selectedHour != null && selectedMinute != null) {
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour!!)
                    calendar.set(Calendar.MINUTE, selectedMinute!!)
                    timeFormatter.format(calendar.time)
                } else {
                    stringResource(R.string.select_time)
                }
                Text(
                    text = timeText,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Visible,
                )
            }
            Button(
                onClick = {
                    onSaveTask(selectedDateMillis, selectedHour, selectedMinute)
                }, enabled = isSaveEnabled, modifier = Modifier.weight(1.5f)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontSize = 12.sp
                )
            }

            TextButton(
                onClick = { showDatePickerDialog = true }, modifier = Modifier.weight(1.2f)
            ) {
                val dateText = selectedDateMillis?.let {
                    dateFormatter.format(it)
                } ?: stringResource(R.string.select_date)
                Text(
                    text = dateText,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Visible,
                )
            }
        }
    }
}
