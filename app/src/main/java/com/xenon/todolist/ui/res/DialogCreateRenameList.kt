package com.xenon.todolist.ui.res

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.xenon.todolist.R

@Composable
fun DialogCreateRenameList(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    title: String,
    confirmButtonText: String,
    initialName: String = ""
) {
    if (showDialog) {
        var textState by remember(initialName) { mutableStateOf(initialName) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                XenonTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = stringResource(R.string.list_name_label),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences,
                        autoCorrectEnabled = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        if (textState.isNotBlank()) {
                            onSave(textState)
                        }
                    },
                    enabled = textState.isNotBlank()
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
