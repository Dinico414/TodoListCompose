package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var showMoreOptions by remember { mutableStateOf(false) }

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

        // More/Less Options Button
        Row(
            modifier = Modifier.fillMaxWidth(), // Make the Row take full width
            horizontalArrangement = Arrangement.End // Align content to the end (right)
        ) {
            TextButton(
                onClick = { showMoreOptions = !showMoreOptions },
                // Remove fillMaxWidth from TextButton if you want it to wrap content
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    // horizontalArrangement = Arrangement.Center // Not needed here anymore
                ) {
                    Text(
                        text = if (showMoreOptions) stringResource(R.string.less_options)
                        else stringResource(R.string.more_options)
                    )
                    Icon(
                        imageVector = if (showMoreOptions) Icons.Filled.ArrowDropUp
                        else Icons.Filled.ArrowDropDown,
                        contentDescription = if (showMoreOptions) stringResource(R.string.less_options)
                        else stringResource(R.string.more_options)
                    )
                }
            }
        }

        // Conditionally display more options here based on showMoreOptions state
        if (showMoreOptions) {
            // Add your additional options UI here
            Text("More options content...")
            Spacer(modifier = Modifier.height(16.dp))
        }

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