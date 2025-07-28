package com.xenon.todolist.ui.res

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DialogTaskItemFiltering(
    onDismissRequest: () -> Unit,
    // Add other parameters your dialog needs, e.g., current filters, callbacks for applying filters
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Filter Tasks") },
        text = {
            // Your filtering options UI goes here
            Text("Filtering options will be here soon.")
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}