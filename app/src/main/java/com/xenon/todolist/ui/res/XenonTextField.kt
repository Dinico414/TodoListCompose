package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import com.xenon.todolist.ui.values.SmallCornerRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XenonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = keyboardOptions,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SmallCornerRadius)),
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