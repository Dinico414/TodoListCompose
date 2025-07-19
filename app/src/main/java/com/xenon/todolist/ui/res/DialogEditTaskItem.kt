package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R
import com.xenon.todolist.viewmodel.classes.TaskItem

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

    XenonDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.edit_task_label),
        properties = DialogProperties(usePlatformDefaultWidth = true),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        TaskItemContent(
            textState = textState,
            onTextChange = { textState = it },
            descriptionState = descriptionState,
            onDescriptionChange = { descriptionState = it },
            currentPriority = currentPriority,
            onPriorityChange = { newPriority -> currentPriority = newPriority },
            initialDueDateMillis = taskItem.dueDateMillis,
            initialDueTimeHour = taskItem.dueTimeHour,
            initialDueTimeMinute = taskItem.dueTimeMinute,
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
                    dueTimeMinute = newMinute
                )
                onConfirm(updatedItem)
            },
            isSaveEnabled = textState.isNotBlank(),
            modifier = Modifier
        )
    }
}
