package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargePadding
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

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.edit_task_label)) },
        text = {
            TaskItemContent(
                textState = textState,
                onTextChange = { textState = it },
                descriptionState = descriptionState,
                onDescriptionChange = { descriptionState = it },
                currentPriority = currentPriority,
                onPriorityChange = { newPriority -> currentPriority = newPriority },
                onSaveTask = { },
                isSaveEnabled = textState.isNotBlank()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedItem = taskItem.copy(
                        task = textState.trim(),
                        description = descriptionState.trim().takeIf { it.isNotBlank() },
                        priority = currentPriority
                    )
                    onConfirm(updatedItem)
                },
                enabled = textState.isNotBlank()
            ) {
                Text(stringResource(R.string.save_task))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = true),
        modifier = Modifier.padding(LargePadding)
    )
}
