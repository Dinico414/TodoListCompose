package com.xenon.todolist.ui.res

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.xenon.todolist.R

@Composable
fun DialogClearDataConfirmation(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.clear_data_dialog_title),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        },
        text = {
            Text(
                text = stringResource(R.string.clear_data_dialog_description),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(
                    stringResource(R.string.confirm)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
    )
}