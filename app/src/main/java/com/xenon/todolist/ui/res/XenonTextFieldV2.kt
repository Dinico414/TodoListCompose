package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Box // Import Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun XenonTextFieldV2(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(100.0f),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.primary.copy(alpha = 0.25f),
            unfocusedContainerColor = colorScheme.primary.copy(alpha = 0.25f),
            focusedIndicatorColor = colorScheme.primary,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = colorScheme.onPrimaryContainer,
            unfocusedTextColor = colorScheme.onPrimaryContainer,
            focusedLabelColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.primary,
        )
    )
}
