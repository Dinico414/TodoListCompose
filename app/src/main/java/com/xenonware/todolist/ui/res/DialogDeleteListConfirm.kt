package com.xenonware.todolist.ui.res

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.xenon.mylibrary.res.XenonDialog
import com.xenonware.todolist.R

@Composable
fun DialogDeleteListConfirm(
    showDialog: Boolean,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (showDialog) {
        val haptic = LocalHapticFeedback.current
        LaunchedEffect(Unit) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        val textColor = MaterialTheme.colorScheme.onErrorContainer

        XenonDialog(
            onDismissRequest = onDismiss,
            title = stringResource(R.string.confirm_delete_title),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            dismissIconButtonContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            dismissIconButtonContentColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
            confirmContainerColor = MaterialTheme.colorScheme.error,
            confirmContentColor = MaterialTheme.colorScheme.onError,
            confirmButtonText = stringResource(R.string.delete),
            onConfirmButtonClick = { onConfirm() },
            properties = DialogProperties(usePlatformDefaultWidth = true),
            contentManagesScrolling = false,
        ) {
            Text(
                text = message,
                color = textColor,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        }
    }
}
