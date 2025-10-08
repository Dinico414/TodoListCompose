package com.xenonware.todolist.ui.res

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf // For managing the list of steps
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R
import com.xenonware.todolist.viewmodel.classes.TaskItem
import com.xenonware.todolist.viewmodel.classes.TaskStep // Import TaskStep
import java.util.UUID // For generating temporary IDs if needed for new steps before confirm

@Composable
fun DialogEditTaskItem(
    taskItem: TaskItem,
    onDismissRequest: () -> Unit,
    onConfirm: (TaskItem) -> Unit

) {
    var textState by remember(taskItem.id) { mutableStateOf(taskItem.task) }
    var descriptionState by remember(taskItem.id) { mutableStateOf(taskItem.description ?: "") }
    var currentPriority by remember(taskItem.id) { mutableStateOf(taskItem.priority) }

    var selectedDueDateMillis by remember(taskItem.id) { mutableStateOf(taskItem.dueDateMillis) }
    var selectedDueTimeHour by remember(taskItem.id) { mutableStateOf(taskItem.dueTimeHour) }
    var selectedDueTimeMinute by remember(taskItem.id) { mutableStateOf(taskItem.dueTimeMinute) }

    val currentSteps = remember(taskItem.id) { mutableStateListOf<TaskStep>() }

    LaunchedEffect(taskItem.steps, taskItem.id) {
        if (currentSteps.toList() != taskItem.steps) {
            currentSteps.clear()
            currentSteps.addAll(taskItem.steps)
        }
    }

    XenonDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.edit_task_label),
        properties = DialogProperties(usePlatformDefaultWidth = true),
        contentPadding = PaddingValues(horizontal = 0.dp),
        contentManagesScrolling = true,
    ) {
        TaskItemContent(
            textState = textState,
            onTextChange = { textState = it },
            descriptionState = descriptionState,
            onDescriptionChange = { descriptionState = it },
            currentPriority = currentPriority,
            onPriorityChange = { newPriority -> currentPriority = newPriority },
            initialDueDateMillis = selectedDueDateMillis,
            initialDueTimeHour = selectedDueTimeHour,
            initialDueTimeMinute = selectedDueTimeMinute,

            currentSteps = currentSteps.toList(),

            onStepAdded = { stepText ->
                val newStep = TaskStep(
                    id = UUID.randomUUID().toString(),
                    text = stepText.trim(),
                    isCompleted = false,
                    displayOrder = currentSteps.size
                )
                currentSteps.add(newStep)
            },
            onStepToggled = { stepId ->
                val stepIndex = currentSteps.indexOfFirst { it.id == stepId }
                if (stepIndex != -1) {
                    val step = currentSteps[stepIndex]
                    currentSteps[stepIndex] = step.copy(isCompleted = !step.isCompleted)
                }
            },
            onStepTextUpdated = { stepId, newText ->
                val stepIndex = currentSteps.indexOfFirst { it.id == stepId }
                if (stepIndex != -1) {
                    currentSteps[stepIndex] = currentSteps[stepIndex].copy(text = newText.trim())
                }
            },
            onStepRemoved = { stepId ->
                currentSteps.removeAll { it.id == stepId }
            },

            onSaveTask = { newDateMillis, newHour, newMinute ->
                selectedDueDateMillis = newDateMillis
                selectedDueTimeHour = newHour
                selectedDueTimeMinute = newMinute

                val updatedItem = taskItem.copy(
                    task = textState.trim(),
                    description = descriptionState.trim().takeIf { it.isNotBlank() },
                    priority = currentPriority,
                    dueDateMillis = newDateMillis,
                    dueTimeHour = newHour,
                    dueTimeMinute = newMinute,
                    steps = currentSteps.toList()
                )
                onConfirm(updatedItem)
            },
            isSaveEnabled = textState.isNotBlank(),
            modifier = Modifier
        )
    }
}
