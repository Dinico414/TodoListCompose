package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun XenonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        keyboardOptions = keyboardOptions,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.0f)),
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