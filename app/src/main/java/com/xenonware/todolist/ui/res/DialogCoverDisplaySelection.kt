package com.xenonware.todolist.ui.res

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
// Removed: import androidx.compose.foundation.selection.selectableGroup // Not directly used in the refactored content structure for XenonDialog
// Removed: import androidx.compose.material3.AlertDialog
// Removed: import androidx.compose.material3.Button
// Removed: import androidx.compose.material3.FilledTonalButton // Was not used
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
// Removed: import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties // Added
import com.xenonware.todolist.R

@Composable
fun DialogCoverDisplaySelection(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    XenonDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.cover_screen_dialog_title),
        confirmButtonText = stringResource(R.string.yes),
        onConfirmButtonClick = { onConfirm() },
        actionButton2Text = stringResource(R.string.no),
        onActionButton2Click = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = true),
        contentManagesScrolling = false,
    ) {
        val containerSize = LocalWindowInfo.current.containerSize
        Column {
            Text(
                text = stringResource(R.string.cover_dialog_description),
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Screen size: ${containerSize.width} x ${containerSize.height} px",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
