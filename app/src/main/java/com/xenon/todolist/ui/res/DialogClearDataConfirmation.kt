package com.xenon.todolist.ui.res

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R


@Composable
fun DialogClearDataConfirmation(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val textColor = MaterialTheme.colorScheme.onErrorContainer

    XenonDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.clear_data_dialog_title),

        containerColor = MaterialTheme.colorScheme.errorContainer,

        confirmContainerColor = MaterialTheme.colorScheme.error,
        confirmContentColor = MaterialTheme.colorScheme.onError,
        confirmButtonText = stringResource(R.string.confirm),
        onConfirmButtonClick = {
            onConfirm()
        },

        properties = DialogProperties(usePlatformDefaultWidth = true),

    ) {
        Text(
            text = stringResource(R.string.clear_data_dialog_description),
            color = textColor)
    }
}

