package com.xenon.todolist.ui.res

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargerPadding
import com.xenon.todolist.ui.values.LargestPadding
import com.xenon.todolist.viewmodel.SortOption
import com.xenon.todolist.viewmodel.SortOrder


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


@OptIn(ExperimentalMaterial3Api::class)
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
    val sortOrderOptions = SortOrder.entries

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
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                sortOrderOptions.forEachIndexed { index, order ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = sortOrderOptions.size
                        ),
                        onClick = { selectedOrder = order },
                        selected = selectedOrder == order,
                        icon = {
                            when (order) {
                                SortOrder.ASCENDING -> Icon(
                                    painter = painterResource(id = R.drawable.sort_ascending),
                                    contentDescription = stringResource(id = R.string.ascending_label)
                                )
                                SortOrder.DESCENDING -> Icon(
                                    painter = painterResource(id = R.drawable.sort_descending),
                                    contentDescription = stringResource(id = R.string.descending_label)
                                )
                            }
                        }
                    ) {
                        Text(
                            text = when (order) {
                                SortOrder.ASCENDING -> stringResource(R.string.ascending_label)
                                SortOrder.DESCENDING -> stringResource(R.string.descending_label)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(LargestPadding))

            Column(Modifier.selectableGroup()) {
                sortOptions.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(100.0f))
                            .selectable(
                                selected = (option == selectedOption),
                                onClick = { selectedOption = option },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = { selectedOption = option }
                        )
                        Spacer(Modifier.width(LargerPadding))
                        Text(
                            text = option.toDisplayString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

