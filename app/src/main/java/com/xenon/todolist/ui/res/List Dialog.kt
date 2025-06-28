// Create a new file, e.g., ListDialogs.kt

package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun ListNameDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    initialName: String = "",
    title: String,
    confirmButtonText: String,
) {
    if (showDialog) {
        var text by remember(initialName) { mutableStateOf(initialName) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.list_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            onSave(text)
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message)) },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}