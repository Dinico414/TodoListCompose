@file:Suppress("DEPRECATION")

package com.xenonware.todolist.ui.res

import android.text.format.DateFormat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xenon.mylibrary.res.XenonTextField
import com.xenon.mylibrary.theme.QuicksandTitleVariable
import com.xenon.mylibrary.values.LargePadding
import com.xenonware.todolist.R
import com.xenonware.todolist.viewmodel.classes.Priority
import com.xenonware.todolist.viewmodel.classes.TaskStep
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.abs

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
)
@Composable
fun TaskSheet(
    onDismiss: () -> Unit,
    onSave: (
        task: String,
        description: String?,
        priority: Priority,
        dueDateMillis: Long?,
        dueTimeHour: Int?,
        dueTimeMinute: Int?,
        steps: List<TaskStep>,
    ) -> Unit,
    initialTask: String = "",
    initialDescription: String? = null,
    initialPriority: Priority = Priority.LOW,
    initialDueDateMillis: Long? = null,
    initialDueTimeHour: Int? = null,
    initialDueTimeMinute: Int? = null,
    initialSteps: List<TaskStep> = emptyList(),
    saveTrigger: Boolean = false,
    onSaveTriggerConsumed: () -> Unit = {},
    toolbarHeight: Dp = 72.dp,
    isBlackThemeActive: Boolean = false,
    isCoverModeActive: Boolean = false,
    onTaskTitleChange: (String) -> Unit = {},
    showDatePicker: Boolean = false,
    showTimePicker: Boolean = false,
    onDatePickerDismiss: () -> Unit = {},
    onTimePickerDismiss: () -> Unit = {},
    onDateChange: (Long?) -> Unit = {},
    onTimeChange: (Int?, Int?) -> Unit = { _, _ -> },
) {
    val hazeState = remember { HazeState() }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var taskTitle by rememberSaveable { mutableStateOf(initialTask) }
    var description by rememberSaveable { mutableStateOf(initialDescription.orEmpty()) }
    var priority by rememberSaveable { mutableStateOf(initialPriority) }
    var selectedDate by rememberSaveable { mutableStateOf(initialDueDateMillis) }
    var selectedHour by rememberSaveable { mutableStateOf(initialDueTimeHour) }
    var selectedMinute by rememberSaveable { mutableStateOf(initialDueTimeMinute) }
    val steps = rememberSaveable(initialSteps) { initialSteps.toMutableStateList() }

    val is24Hour = DateFormat.is24HourFormat(context)

    // Report title changes live to parent
    LaunchedEffect(taskTitle) {
        onTaskTitleChange(taskTitle)
    }

    // Auto-save trigger
    LaunchedEffect(saveTrigger) {
        if (saveTrigger) {
            onSave(
                taskTitle.trim(),
                description.trim().takeIf { it.isNotBlank() },
                priority,
                selectedDate,
                selectedHour,
                selectedMinute,
                steps.toList()
            )
            onSaveTriggerConsumed()
        }
    }

    // Reset local steps when initial changes (edit mode)
    LaunchedEffect(initialSteps) {
        if (steps.toList() != initialSteps) {
            steps.clear()
            steps.addAll(initialSteps)
        }
    }

    val hazeThinColor = colorScheme.surfaceDim

    val safeDrawingPadding = if (WindowInsets.ime.asPaddingValues()
            .calculateBottomPadding() > WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
            .asPaddingValues().calculateBottomPadding()
    ) {
        WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    } else {
        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()
            .calculateBottomPadding()
    }

    val bottomPadding = safeDrawingPadding + toolbarHeight + 16.dp
    val backgroundColor =
        if (isCoverModeActive || isBlackThemeActive) Color.Black else colorScheme.surfaceContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        val topPadding = 68.dp
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(top = 4.dp)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .hazeSource(hazeState)
        ) {
            item {
                Spacer(modifier = Modifier.height(topPadding + LargePadding))

                // ── PRIORITY ───────────────────────────────────────
                Text(
                    text = stringResource(id = R.string.priority_label), style = typography.titleMedium.copy(
                        fontFamily = QuicksandTitleVariable, fontWeight = FontWeight.Light
                    ), modifier = Modifier.padding(bottom = 8.dp)
                )

                XenonSingleChoiceButtonGroup(
                    options = Priority.entries.toList(),
                    selectedOption = priority,
                    onOptionSelect = { priority = it },
                    label = {
                        when (it) {
                            Priority.LOW -> stringResource(id = R.string.priority_low)
                            Priority.HIGH -> stringResource(id = R.string.priority_high)
                            Priority.HIGHEST -> stringResource(id = R.string.priority_highest)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(LargePadding))

                // ── DESCRIPTION ──────────────────────────────────────────────────
                Text(
                    text = stringResource(id = R.string.task_description_label), style = typography.titleMedium.copy(
                        fontFamily = QuicksandTitleVariable, fontWeight = FontWeight.Light
                    ), modifier = Modifier.padding(bottom = 8.dp)
                )
                XenonTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text(stringResource(id = R.string.task_description_label)) },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(LargePadding))

                // ── STEPS ────────────────────────────────────────────────────────
                Text(
                    text = stringResource(id = R.string.steps), style = typography.titleMedium.copy(
                        fontFamily = QuicksandTitleVariable, fontWeight = FontWeight.Light
                    ), modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                var newStepText by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    XenonTextField(
                        value = newStepText,
                        onValueChange = { newStepText = it },
                        placeholder = { Text(stringResource(id = R.string.add_new_step)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledIconButton(
                        onClick = {
                            if (newStepText.isNotBlank()) {
                                steps.add(
                                    TaskStep(
                                        id = java.util.UUID.randomUUID().toString(),
                                        text = newStepText.trim(),
                                        displayOrder = steps.size,
                                        isCompleted = false
                                    )
                                )
                                newStepText = ""
                            }
                        }, enabled = newStepText.isNotBlank(), modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add step")
                    }
                }
                Spacer(modifier = Modifier.height(LargePadding))
            }

            if (steps.isNotEmpty()) {
                items(steps, key = { it.id }) { step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = step.isCompleted, onCheckedChange = {
                                val idx = steps.indexOf(step)
                                if (idx >= 0) {
                                    steps[idx] = step.copy(isCompleted = it)
                                }
                            })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step.text,
                            modifier = Modifier.weight(1f),
                            style = if (step.isCompleted) {
                                typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                typography.bodyMedium
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = { steps.remove(step) }) {
                            Icon(
                                Icons.Default.Delete, contentDescription = "Remove step"
                            )
                        }
                    }
                    if (step != steps.last()) {
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    }
                }
            } else {
                item {
                    Text(
                        text = stringResource(id = R.string.no_steps),
                        style = typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(bottomPadding))
            }
        }

        // Toolbar – title only
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(100f))
                .background(colorScheme.surfaceDim)
                .hazeEffect(
                    state = hazeState, style = HazeMaterials.ultraThin(hazeThinColor)
                ), verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss, modifier = Modifier.padding(4.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }

            val titleTextStyle = typography.titleLarge.merge(
                TextStyle(
                    fontFamily = QuicksandTitleVariable,
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurface
                )
            )

            BasicTextField(
                value = taskTitle,
                onValueChange = {
                    taskTitle = it
                    onTaskTitleChange(it)
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = titleTextStyle,
                cursorBrush = SolidColor(colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (taskTitle.isEmpty()) {
                            Text(
                                text = "Title",
                                style = titleTextStyle,
                                color = colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    }
                })

            Box {
                IconButton(
                    onClick = { /* More options placeholder */ }, modifier = Modifier.padding(4.dp)
                ) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
                }
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(onDismissRequest = onDatePickerDismiss, confirmButton = {
            TextButton(onClick = {
                selectedDate = dateState.selectedDateMillis
                onDateChange(dateState.selectedDateMillis)
                onDatePickerDismiss()
            }) { Text(stringResource(id = R.string.ok)) }
        }, dismissButton = {
            TextButton(onClick = onDatePickerDismiss) { Text(stringResource(id = R.string.cancel)) }
        }) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = selectedHour ?: calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedMinute ?: calendar.get(Calendar.MINUTE),
            is24Hour = is24Hour
        )

        AlertDialog(
            onDismissRequest = onTimePickerDismiss,
            title = { Text(stringResource(R.string.select_time_title)) },
            text = {
                TimePicker(state = timeState)
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timeState.hour
                    selectedMinute = timeState.minute
                    onTimeChange(timeState.hour, timeState.minute)
                    onTimePickerDismiss()
                }) { Text(stringResource(id = R.string.ok)) }

            },
            dismissButton = {
                TextButton(onClick = onTimePickerDismiss) { Text(stringResource(id = R.string.cancel)) }
            })
    }

}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> XenonSingleChoiceButtonGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    colors: ToggleButtonColors = ToggleButtonDefaults.toggleButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        checkedContainerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onSurface,
        checkedContentColor = MaterialTheme.colorScheme.onPrimary
    ),
    icon: @Composable (T, Boolean) -> Unit = { _, isSelected ->
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp)
            )
        }
    }
) {
    val interactionSources = remember(options) { options.map { MutableInteractionSource() } }
    
    val pressedStates = remember(options) { 
        mutableStateListOf<Boolean>().apply { repeat(options.size) { add(false) } } 
    }

    options.forEachIndexed { index, _ ->
        LaunchedEffect(interactionSources[index]) {
            var pressStartTime = 0L
            interactionSources[index].interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        pressedStates[index] = true
                        pressStartTime = System.currentTimeMillis()
                    }
                    is PressInteraction.Release -> {
                        val duration = System.currentTimeMillis() - pressStartTime
                        if (duration < 200) {
                            delay(200 - duration)
                        }
                        pressedStates[index] = false
                    }
                    is PressInteraction.Cancel -> {
                        pressedStates[index] = false
                    }
                }
            }
        }
    }

    val pressedIndex = pressedStates.indexOfFirst { it }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = selectedOption == option
            
            val targetWeight = if (pressedIndex == -1) {
                1f
            } else {
                if (index == pressedIndex) {
                    1.15f
                } else if (abs(index - pressedIndex) == 1) {
                    val neighbors = if (pressedIndex == 0 || pressedIndex == options.size - 1) 1 else 2
                    1f - (0.15f / neighbors)
                } else {
                    1f
                }
            }

            val weight by animateFloatAsState(
                targetValue = targetWeight,
                label = "weight",
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )

            ToggleButton(
                checked = isSelected,
                onCheckedChange = { if (it) onOptionSelect(option) },
                modifier = Modifier.weight(weight),
                colors = colors,
                interactionSource = interactionSources[index]
            ) {
                icon(option, isSelected)
                Text(
                    text = label(option),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
