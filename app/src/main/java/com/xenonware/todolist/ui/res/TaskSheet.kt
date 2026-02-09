@file:Suppress("DEPRECATION")

package com.xenonware.todolist.ui.res

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.xenonware.todolist.viewmodel.classes.Priority
import com.xenonware.todolist.viewmodel.classes.TaskStep
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import java.util.Calendar

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
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(top = 4.dp)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .verticalScroll(scrollState)
                .hazeSource(hazeState)
        ) {
            Spacer(modifier = Modifier.height(topPadding))

            // ── DESCRIPTION ──────────────────────────────────────────────────
            Text(
                text = "Description", style = typography.titleMedium.copy(
                    fontFamily = QuicksandTitleVariable, fontWeight = FontWeight.Light
                ), modifier = Modifier.padding(bottom = 8.dp)
            )
            XenonTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Add details...") },
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── IMPORTANCE / PRIORITY ───────────────────────────────────────
            Text(
                text = "Importance", style = typography.titleMedium.copy(
                    fontFamily = QuicksandTitleVariable, fontWeight = FontWeight.Light
                ), modifier = Modifier.padding(bottom = 8.dp)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Priority.entries.forEachIndexed { index, p ->
                    SegmentedButton(
                        selected = priority == p,
                        onClick = { priority = p },
                        shape = SegmentedButtonDefaults.itemShape(
                            index, Priority.entries.size
                        )
                    ) {
                        Text(
                            text = when (p) {
                                Priority.LOW -> "Low"
                                Priority.HIGH -> "High"
                                Priority.HIGHEST -> "Highest"
                            }
                        )
                    }
                }
            }

            SingleChoiceButtonGroup()

            Spacer(modifier = Modifier.height(32.dp))

            // ── STEPS ────────────────────────────────────────────────────────
            Text(
                text = "Steps", style = typography.titleMedium.copy(
                    fontFamily = QuicksandTitleVariable, fontWeight = FontWeight.Light
                ), modifier = Modifier.padding(bottom = 8.dp)
            )

            var newStepText by remember { mutableStateOf("") }

            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                XenonTextField(
                    value = newStepText,
                    onValueChange = { newStepText = it },
                    placeholder = { Text("Add new step") },
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

            Spacer(modifier = Modifier.height(16.dp))

            if (steps.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
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
                }
            } else {
                Text(
                    text = "No steps yet",
                    style = typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(bottomPadding))
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
            }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = onDatePickerDismiss) { Text("Cancel") }
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
        DatePickerDialog(onDismissRequest = onTimePickerDismiss, confirmButton = {
            TextButton(onClick = {
                selectedHour = timeState.hour
                selectedMinute = timeState.minute
                onTimeChange(timeState.hour, timeState.minute)
                onTimePickerDismiss()
            }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = onTimePickerDismiss) { Text("Cancel") }
        }) {
            TimePicker(state = timeState)
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SingleChoiceButtonGroup(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    ButtonGroup(
        overflowIndicator = {
            FilledTonalIconButton(
                onClick = {
                    it.show()
                }
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
        }) {
        for (i in 0 until 3) {
            val checked = i == selectedIndex

            this.toggleableItem(
                checked = checked,
                label = "Item $i",
                weight = 1f,
                onCheckedChange = { selectedIndex = i },
                icon = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected"
                        )
                    }
                } else null
            )
        }
    }
}