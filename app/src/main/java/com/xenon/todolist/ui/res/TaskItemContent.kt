package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargePadding

@Composable
fun TaskItemContent(
    textState: String,
    onTextChange: (String) -> Unit,
    onSaveTask: () -> Unit,
    isSaveEnabled: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(LargePadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        XenonTextField(
            value = textState,
            onValueChange = onTextChange,
            label = stringResource(R.string.new_task_label),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSaveTask,
            enabled = isSaveEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_task))
        }
        Spacer(modifier = Modifier.height(LargePadding))
    }
}