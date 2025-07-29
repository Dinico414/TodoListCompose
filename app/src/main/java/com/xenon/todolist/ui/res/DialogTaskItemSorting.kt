package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargerPadding
import com.xenon.todolist.viewmodel.SortOption
import com.xenon.todolist.viewmodel.SortOrder
import kotlin.text.replaceFirstChar


fun SortOption.toDisplayString(): String {
    val lowercaseName = this.name.replace("_", " ").toLowerCase(Locale.current)
    if (lowercaseName.isEmpty()) {
        return ""
    }
    return lowercaseName.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecaseChar()
        } else {
            it
        }
    }
}


@Composable
fun DialogTaskItemSorting(
    currentSortOption: SortOption,
    currentSortOrder: SortOrder,
    onDismissRequest: () -> Unit,
    onApplySort: (SortOption, SortOrder) -> Unit
) {
    var selectedOption by remember { mutableStateOf(currentSortOption) }
    var selectedOrder by remember { mutableStateOf(currentSortOrder) }

    val sortOptions = SortOption.entries

    XenonDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(id = R.string.sort_tasks_description),
        confirmButtonText = stringResource(id = R.string.ok),
        onConfirmButtonClick = {
            onApplySort(selectedOption, selectedOrder)
            onDismissRequest()
        },
        properties = DialogProperties(usePlatformDefaultWidth = true),
        contentManagesScrolling = false
    ) {
        Column {
            Column(Modifier.selectableGroup()) {
                sortOptions.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (option == selectedOption),
                                onClick = { selectedOption = option },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = LargerPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = null
                        )
                        Text(
                            text = option.toDisplayString(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = LargerPadding)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(
                    onClick = { selectedOrder = SortOrder.ASCENDING },
                    modifier = Modifier.weight(1f),
                    colors = if (selectedOrder == SortOrder.ASCENDING) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Icon(
                        Icons.Filled.ArrowUpward,
                        contentDescription = stringResource(id = R.string.ascending_label),
                        tint = if (selectedOrder == SortOrder.ASCENDING) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.ascending_label))
                }
                OutlinedButton(
                    onClick = { selectedOrder = SortOrder.DESCENDING },
                    modifier = Modifier.weight(1f),
                    colors = if (selectedOrder == SortOrder.DESCENDING) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Icon(
                        Icons.Filled.ArrowDownward,
                        contentDescription = stringResource(id = R.string.descending_label),
                        tint = if (selectedOrder == SortOrder.DESCENDING) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.descending_label))
                }
            }
        }
    }
}
