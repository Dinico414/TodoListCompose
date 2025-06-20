package com.xenon.todolist.ui.res

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
import com.xenon.todolist.R
import com.xenon.todolist.viewmodel.classes.TodoItem

@Composable
fun DialogEditTaskItem(
    taskItem: TodoItem,
    onDismissRequest: () -> Unit,
    onConfirm: (TodoItem) -> Unit,
    modifier: Modifier = Modifier
) {

    var currentTaskText by remember(taskItem.task) { mutableStateOf(taskItem.task) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.edit_task_label))
        },
        text = {
            TaskItemContent(
                textState = currentTaskText,
                onTextChange = { newText ->
                    currentTaskText = newText
                },
                onSaveTask = {
                },
                isSaveEnabled = currentTaskText.isNotBlank()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(taskItem.copy(task = currentTaskText))
                },
                enabled = currentTaskText.isNotBlank()
            ) {
                Text(stringResource(R.string.save_task))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}