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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.viewmodel.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemContent(
    textState: String,
    onTextChange: (String) -> Unit,
    descriptionState: String,
    onDescriptionChange: (String) -> Unit,
    currentPriority: Priority,
    onPriorityChange: (Priority) -> Unit,
    onSaveTask: () -> Unit,
    isSaveEnabled: Boolean,
) {
    var showMoreOptions by remember { mutableStateOf(false) }
    val priorityOptions = Priority.values()

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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(MediumPadding))

        XenonTextField(
            value = descriptionState,
            onValueChange = onDescriptionChange,
            label = stringResource(R.string.task_description_label),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = false,
            maxLines = 5
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { showMoreOptions = !showMoreOptions },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
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

        if (showMoreOptions) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.priority_label),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = MediumPadding / 2)
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorityOptions.forEachIndexed { index, priority ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = priorityOptions.size
                            ),
                            onClick = { onPriorityChange(priority) },
                            selected = currentPriority == priority
                        ) {
                            Text(
                                text = when (priority) {
                                    Priority.LOW -> stringResource(R.string.priority_low)
                                    Priority.HIGH -> stringResource(R.string.priority_high)
                                    Priority.HIGHEST -> stringResource(R.string.priority_highest)
                                }
                            )
                        }
                    }
                }
            }
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